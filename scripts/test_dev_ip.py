import importlib.machinery
import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from concurrent.futures import ThreadPoolExecutor
from unittest.mock import patch

loader = importlib.machinery.SourceFileLoader('dev_ip', str(Path(__file__).with_name('bombom-dev-ip')))
spec = importlib.util.spec_from_loader(loader.name, loader)
dev_ip = importlib.util.module_from_spec(spec)
loader.exec_module(dev_ip)


class DevIpTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        root = Path(self.directory.name)
        self.map_file = root / 'ips.map'
        self.map_file.write_text('8.8.8.8 "samesite=none"; # existing\n')
        self.state_directory = root / 'state'
        self.state_file = self.state_directory / 'users.json'
        for name, value in [('MAP_FILE', self.map_file), ('STATE_DIRECTORY', self.state_directory),
                            ('STATE_FILE', self.state_file)]:
            self.patch_value(dev_ip, name, value)
        self.patch_value(dev_ip.os, 'geteuid', lambda: 0)
        self.command = self.patch_value(dev_ip, 'run')

    def patch_value(self, target, name, value=None):
        replacement = patch.object(target, name, value) if value is not None else patch.object(target, name)
        result = replacement.start()
        self.addCleanup(replacement.stop)
        return result

    def users(self):
        return json.loads(self.state_file.read_text())

    def test_첫_등록은_기존_수동_IP를_보존한다(self):
        dev_ip.manage('register', 'Alice', '1.1.1.1')
        self.assertEqual(self.users(), {'legacy:8.8.8.8': '8.8.8.8', 'alice': '1.1.1.1'})
        self.assertEqual(self.map_file.read_text(), '1.1.1.1 "samesite=none";\n8.8.8.8 "samesite=none";\n')
        self.assertEqual(self.state_file.stat().st_mode & 0o777, 0o600)

    def test_기존_IP를_인계받은_후_삭제한다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8')
        dev_ip.manage('remove', 'alice', '')
        self.assertEqual(self.users(), {})
        self.assertEqual(self.map_file.read_text(), '')

    def test_재등록은_같은_사용자의_이전_IP를_교체한다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8')
        dev_ip.manage('register', 'ALICE', '1.1.1.1')
        self.assertEqual(self.users(), {'alice': '1.1.1.1'})
        self.assertNotIn('8.8.8.8', self.map_file.read_text())

    def test_공유_IP는_다른_등록자가_남아있으면_유지한다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8')
        dev_ip.manage('register', 'bob', '8.8.8.8')
        dev_ip.manage('remove', 'alice', '')
        self.assertEqual(self.users(), {'bob': '8.8.8.8'})
        self.assertEqual(self.map_file.read_text().count('8.8.8.8'), 1)

    def test_잘못된_IP와_셸_문법은_파일을_변경하지_않는다(self):
        original = self.map_file.read_bytes()
        for address in ['', '127.0.0.1', '192.168.1.1', '224.0.0.1', '::1', '8.8.8.8;id', '8.8.8.8\n1.1.1.1']:
            with self.subTest(address=address):
                with self.assertRaises(ValueError):
                    dev_ip.manage('register', 'alice', address)
                self.assertEqual(self.map_file.read_bytes(), original)
        self.assertFalse(self.state_file.exists())
        self.command.assert_not_called()

    def test_다른_작업과_잘못된_사용자명은_거부한다(self):
        for arguments in [('delete-all', 'alice', ''), ('register', '../root', '8.8.8.8'),
                          ('remove', 'alice', '8.8.8.8')]:
            with self.subTest(arguments=arguments):
                with self.assertRaises(ValueError):
                    dev_ip.manage(*arguments)
        self.command.assert_not_called()

    def test_Nginx_검사_실패는_기존_파일을_복구하고_reload하지_않는다(self):
        original = self.map_file.read_bytes()
        self.command.side_effect = subprocess.CalledProcessError(1, ['nginx', '-t'])
        with self.assertRaises(subprocess.CalledProcessError):
            dev_ip.manage('register', 'alice', '1.1.1.1')
        self.assertEqual(self.map_file.read_bytes(), original)
        self.assertFalse(self.state_file.exists())
        self.assertEqual(self.command.call_count, 1)

    def test_reload_실패는_등록_상태도_복구하고_기존_설정을_다시_적용한다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8')
        original_map, original_state = self.map_file.read_bytes(), self.state_file.read_bytes()
        self.command.reset_mock()
        self.command.side_effect = [None, subprocess.CalledProcessError(1, ['systemctl']), None, None]
        with self.assertRaises(subprocess.CalledProcessError):
            dev_ip.manage('register', 'alice', '1.1.1.1')
        self.assertEqual(self.map_file.read_bytes(), original_map)
        self.assertEqual(self.state_file.read_bytes(), original_state)
        self.assertEqual(self.command.call_count, 4)

    def test_잘못된_수동_map은_덮어쓰지_않는다(self):
        self.map_file.write_text('include /some/file;\n')
        with self.assertRaises(ValueError):
            dev_ip.manage('register', 'alice', '1.1.1.1')
        self.assertEqual(self.map_file.read_text(), 'include /some/file;\n')
        self.command.assert_not_called()

    def test_동시_등록이_다른_사용자의_등록을_덮어쓰지_않는다(self):
        with ThreadPoolExecutor(max_workers=8) as executor:
            list(executor.map(lambda index: dev_ip.manage('register', f'user-{index}', '8.8.8.8'), range(8)))
        self.assertEqual(len(self.users()), 8)
        self.assertEqual(self.map_file.read_text(), '8.8.8.8 "samesite=none";\n')

    def test_root_권한_없이는_실행하지_않는다(self):
        with patch.object(dev_ip.os, 'geteuid', return_value=1000):
            with self.assertRaises(PermissionError):
                dev_ip.manage('register', 'alice', '1.1.1.1')
        self.command.assert_not_called()


if __name__ == '__main__':
    unittest.main()
