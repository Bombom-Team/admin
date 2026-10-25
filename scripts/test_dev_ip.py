import importlib.machinery
import importlib.util
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
        for name, value in [('MAP_FILE', self.map_file), ('STATE_DIRECTORY', self.state_directory)]:
            self.patch_value(dev_ip, name, value)
        self.patch_value(dev_ip.os, 'geteuid', lambda: 0)
        self.command = self.patch_value(dev_ip, 'run')

    def patch_value(self, target, name, value=None):
        replacement = patch.object(target, name, value) if value is not None else patch.object(target, name)
        result = replacement.start()
        self.addCleanup(replacement.stop)
        return result

    def addresses(self):
        return set(dev_ip.registered_addresses(self.map_file.read_bytes()))

    def test_추가할_때_기존_수동_IP를_보존한다(self):
        dev_ip.manage('register', 'alice', '1.1.1.1', location='테스트')
        self.assertEqual(self.addresses(), {'8.8.8.8', '1.1.1.1'})
        self.assertEqual(self.map_file.stat().st_mode & 0o777, 0o644)

    def test_기존_수동_IP를_재등록_없이_삭제한다(self):
        dev_ip.manage('remove', 'alice', '8.8.8.8')
        self.assertEqual(self.map_file.read_text(), '')

    def test_다른_계정이_추가해도_같은_IP는_중복되지_않는다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8', location='테스트')
        dev_ip.manage('register', 'bob', '8.8.8.8', location='테스트')
        self.assertEqual(self.map_file.read_text(), '8.8.8.8 "samesite=none"; # 테스트\n')

    def test_같은_계정이_새_IP를_추가해도_기존_IP는_유지한다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8', location='테스트')
        dev_ip.manage('register', 'alice', '1.1.1.1', location='테스트')
        self.assertEqual(self.addresses(), {'8.8.8.8', '1.1.1.1'})

    def test_다른_사람이_추가한_IP도_입력한_주소만_삭제한다(self):
        dev_ip.manage('register', 'alice', '1.1.1.1', location='테스트')
        dev_ip.manage('remove', 'bob', '1.1.1.1')
        self.assertEqual(self.addresses(), {'8.8.8.8'})

    def test_없는_IP를_삭제해도_다른_IP는_유지한다(self):
        dev_ip.manage('remove', 'alice', '1.1.1.1')
        self.assertEqual(self.addresses(), {'8.8.8.8'})

    def test_이전_사용자_상태가_삭제한_IP를_복원하지_않는다(self):
        self.state_directory.mkdir()
        old_state = self.state_directory / 'users.json'
        old_state.write_text('{"alice":"8.8.8.8"}')
        dev_ip.manage('remove', 'bob', '8.8.8.8')
        dev_ip.manage('register', 'alice', '1.1.1.1', location='테스트')
        self.assertEqual(self.addresses(), {'1.1.1.1'})
        self.assertEqual(old_state.read_text(), '{"alice":"8.8.8.8"}')

    def test_등록과_삭제_모두_잘못된_IP와_셸_문법을_거부한다(self):
        original = self.map_file.read_bytes()
        for action in ['register', 'remove']:
            for address in ['', '127.0.0.1', '192.168.1.1', '224.0.0.1', '::1', '8.8.8.8;id', '8.8.8.8\n1.1.1.1']:
                with self.subTest(action=action, address=address):
                    with self.assertRaises(ValueError):
                        dev_ip.manage(action, 'alice', address, location='테스트')
                    self.assertEqual(self.map_file.read_bytes(), original)
        self.command.assert_not_called()

    def test_다른_작업과_잘못된_사용자명은_거부한다(self):
        for arguments in [('delete-all', 'alice', '8.8.8.8'), ('register', '../root', '8.8.8.8')]:
            with self.subTest(arguments=arguments):
                with self.assertRaises(ValueError):
                    dev_ip.manage(*arguments, location='테스트')
        self.command.assert_not_called()

    def test_Nginx_검사_실패는_기존_파일을_복구하고_reload하지_않는다(self):
        original = self.map_file.read_bytes()
        self.command.side_effect = subprocess.CalledProcessError(1, ['nginx', '-t'])
        with self.assertRaises(subprocess.CalledProcessError):
            dev_ip.manage('remove', 'alice', '8.8.8.8')
        self.assertEqual(self.map_file.read_bytes(), original)
        self.assertEqual(self.command.call_count, 1)

    def test_reload_실패는_기존_파일을_복구하고_다시_적용한다(self):
        original = self.map_file.read_bytes()
        self.command.side_effect = [None, subprocess.CalledProcessError(1, ['systemctl']), None, None]
        with self.assertRaises(subprocess.CalledProcessError):
            dev_ip.manage('remove', 'alice', '8.8.8.8')
        self.assertEqual(self.map_file.read_bytes(), original)
        self.assertEqual(self.command.call_count, 4)

    def test_잘못된_수동_map은_덮어쓰지_않는다(self):
        self.map_file.write_text('include /some/file;\n')
        with self.assertRaises(ValueError):
            dev_ip.manage('register', 'alice', '1.1.1.1', location='테스트')
        self.assertEqual(self.map_file.read_text(), 'include /some/file;\n')
        self.command.assert_not_called()

    def test_동시_추가한_서로_다른_IP가_누락되지_않는다(self):
        addresses = {f'1.1.1.{index}' for index in range(1, 9)}
        with ThreadPoolExecutor(max_workers=8) as executor:
            list(executor.map(lambda address: dev_ip.manage('register', 'alice', address, location='테스트'), addresses))
        self.assertEqual(self.addresses(), addresses | {'8.8.8.8'})

    def test_root_권한_없이는_실행하지_않는다(self):
        with patch.object(dev_ip.os, 'geteuid', return_value=1000):
            with self.assertRaises(PermissionError):
                dev_ip.manage('register', 'alice', '1.1.1.1', location='테스트')
        self.command.assert_not_called()


    def test_등록한_장소를_한글_주석으로_남긴다(self):
        dev_ip.manage('register', 'alice', '1.1.1.1', '모루 집')
        self.assertIn('1.1.1.1 "samesite=none"; # 모루 집\n', self.map_file.read_text())

    def test_다른_IP를_추가하거나_삭제해도_기존_주석을_보존한다(self):
        dev_ip.manage('register', 'alice', '1.1.1.1', '사무실')
        self.assertIn('8.8.8.8 "samesite=none"; # existing\n', self.map_file.read_text())
        dev_ip.manage('remove', 'alice', '1.1.1.1')
        self.assertEqual(self.map_file.read_text(), '8.8.8.8 "samesite=none"; # existing\n')

    def test_같은_IP의_장소는_재등록으로_수정한다(self):
        dev_ip.manage('register', 'alice', '8.8.8.8', '집')
        dev_ip.manage('register', 'bob', '8.8.8.8', '사무실')
        self.assertEqual(self.map_file.read_text(), '8.8.8.8 "samesite=none"; # 사무실\n')

    def test_장소가_없거나_줄바꿈과_제어문자가_있으면_등록을_거부한다(self):
        original = self.map_file.read_bytes()
        for location in ['', '   ', '집\ninclude /tmp/injected;', '집\r사무실', '집\x00', 'a' * 81]:
            with self.subTest(location=location):
                with self.assertRaises(ValueError):
                    dev_ip.manage('register', 'alice', '1.1.1.1', location)
                self.assertEqual(self.map_file.read_bytes(), original)
        self.command.assert_not_called()

    def test_주석_없는_기존_IP도_보존하고_장소는_80자까지_허용한다(self):
        self.map_file.write_text('8.8.8.8 "samesite=none";\n')
        dev_ip.manage('register', 'alice', '1.1.1.1', '가' * 80)
        self.assertIn('8.8.8.8 "samesite=none";\n', self.map_file.read_text())
        self.assertIn('# ' + '가' * 80, self.map_file.read_text())


if __name__ == '__main__':
    unittest.main()
