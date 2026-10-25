# Dev IP 관리

`Dev IP 관리` 수동 workflow는 dev API의 `JSESSIONID_DEV` 발급에만 사용하는 Nginx SameSite 예외를 관리한다.
AWS 키는 필요 없다. EC2의 전용 `dev-ip` runner가 root 소유의 고정 스크립트를 호출한다.
기존 배포 workflow와 Spring 설정은 변경하지 않는다.

## 서버 최초 설치

이 브랜치의 `scripts/bombom-dev-ip` 파일을 EC2로 복사한 뒤, 그 파일이 있는 디렉터리에서 실행한다.
runner 서비스 계정으로 접속한 상태에서 아래 권한 설정을 수행한다.

```bash
sudo install -o root -g root -m 0755 ./bombom-dev-ip /usr/local/sbin/bombom-dev-ip
sudo install -d -o root -g root -m 0700 /var/lib/bombom-dev-ip
sudo chown root:root /etc/nginx/dev-samesite-ips.map
sudo chmod 0644 /etc/nginx/dev-samesite-ips.map
command -v python3
command -v nginx
command -v systemctl
```

스크립트는 `/usr/bin/python3`, `/usr/sbin/nginx`, `/usr/bin/systemctl`을 사용한다. 경로가 다르면 설치 전에 스크립트 상수를 맞춘다.
스크립트와 상위 `/usr/local/sbin`은 runner가 수정할 수 없는 root 소유여야 한다.

runner 서비스의 실행 계정을 확인한다. 설치 당시 `svc.sh install "$(whoami)"`를 사용했다면 그때의 계정이다.
`sudo visudo -f /etc/sudoers.d/bombom-dev-ip`로 다음 한 줄을 추가한다. `RUNNER_USER`를 실제 계정으로 바꾼다.

```sudoers
RUNNER_USER ALL=(root) NOPASSWD: /usr/local/sbin/bombom-dev-ip
```

```bash
sudo chmod 0440 /etc/sudoers.d/bombom-dev-ip
sudo visudo -cf /etc/sudoers.d/bombom-dev-ip
sudo nginx -t
```

`sudo ALL`이나 runner가 수정할 수 있는 저장소 파일을 root로 실행하는 권한은 추가하지 않는다.
기존 서비스 계정에 이미 무제한 sudo 또는 Docker 권한이 있다면 이 한 줄이 그 권한을 축소하지는 않는다.
허용 계정은 입력한 IP를 모두 관리할 수 있다. 사용자명은 실행 기록용이며 IP 소유자 검증에 사용하지 않는다. 이 sudo 권한을 가진 계정과 workflow를 수정할 수 있는 사람은 등록 관리자로 신뢰해야 한다.

## GitHub 설정

기본 브랜치 `main`에 `.github/workflows/dev-ip.yml`을 반영한다.
Settings → Secrets and variables → Actions → Variables에서 다음 repository variable을 설정한다.

```text
DEV_IP_ALLOWED_USERS=Choidongjun0830,other-user
```

공백 없이 GitHub 사용자명을 쉼표로 구분한다. 원래 실행자와 재실행자 모두 목록에 있어야 한다.
비허용 사용자, 미설정 목록, 기본 브랜치 외 실행은 job을 건너뛴다.
수동 실행에는 저장소 쓰기 권한도 필요하다. 허용 목록을 추가한다고 쓰기 권한까지 부여되지는 않는다.
workflow와 Variable을 바꿀 수 있는 관리자는 실행 제한도 바꿀 수 있다.
공개 저장소의 실행 입력·기록에 IP가 공개될 수 있으며, 로그 마스킹만으로 이를 비공개로 만들 수는 없다.

## 실행

Actions → Dev IP 관리 → Run workflow에서 작업을 선택하고 공인 IPv4 주소를 입력한다.

- `register`: 입력한 IP가 없으면 추가한다. 이미 있으면 중복 추가하지 않는다.
- `remove`: 입력한 IP를 삭제한다. 기존 수동 항목이나 다른 사람이 추가한 IP도 바로 삭제할 수 있다.
- 두 작업 모두 IP 입력이 필수다. 없는 IP 삭제는 다른 항목을 변경하지 않는다.
- 같은 계정도 여러 IP를 추가할 수 있다. 새 IP를 등록해도 이전 IP는 자동 삭제되지 않는다.
- IP를 공유하는 사람이 있더라도 해당 IP를 remove하면 그 주소의 예외가 제거된다.

`/etc/nginx/dev-samesite-ips.map`이 유일한 목록 기준이다.
이전 버전의 `/var/lib/bombom-dev-ip/users.json`은 더 이상 읽거나 수정하지 않는다.
기존 map에 들어 있는 IP는 별도 인계나 재등록 없이 관리할 수 있다.
정상 등록·삭제 모두 파일 잠금, 원자적 교체, `nginx -t`, reload를 거친다. 검사·reload 실패 시 원래 map을 복구한다.

## 이전 버전에서 갱신

workflow만 머지해도 EC2에 설치된 스크립트가 자동 갱신되지는 않는다.
이 PR의 `scripts/bombom-dev-ip`를 서버에 다시 복사해 위 `sudo install` 명령으로 교체한다.
root 소유와 제한된 sudo 권한은 그대로 유지하며, 별도 사용자 등록 상태 파일 이관은 필요 없다.

등록 삭제는 이미 브라우저에 발급된 SameSite=None 쿠키나 서버 세션을 폐기하지 않는다.
현재 CORS 허용 목록은 유지하며, 등록 후 쿠키를 지우고 다시 로그인해 발급 속성을 확인한다.

## 로컬 검증

```bash
python3 scripts/test_dev_ip.py
```

임시 파일과 Nginx 명령 대역으로 IP 추가·중복 방지·기존 수동 IP 삭제·다른 항목 보존·입력 거부·실패 복구를 확인한다.
실제 EC2의 권한, Nginx 검사와 reload, GitHub 수동 실행은 별도 확인이 필요하다.
