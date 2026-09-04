r"""추적 대상 텍스트 파일에 제어문자가 섞이는 것을 막는다.

증상 근거: heredoc/스크립트 편집 경로가 정규식 이스케이프를 실제 제어문자로 바꿔 버린다.
`\b`(워드 경계)가 0x08(백스페이스)로, `\n`이 실제 개행으로 치환되는 사고가 반복됐다.

- Harness 문서에 0x08 이 들어가 self-acceptance 게이트가 CONTROL_CHAR 로 fail-closed 됐다.
- 공개 README 의 `.\bin\cpf-start.ps1` 이 `.<0x08>in\cpf-start.ps1` 이 되어, 그대로 복사한
  사용자가 실행할 수 없는 명령이 배포될 뻔했다.
- 검증기의 정규식이 조용히 다른 패턴이 되어 게이트가 0건을 반환한 적도 있다.

되돌리면 재발할 증상: 눈으로는 정상으로 보이는 문서/스크립트가 실제로는 깨진 바이트를 갖고,
게이트가 조용히 통과하거나 사용자가 복사한 명령이 동작하지 않는다.
"""

from __future__ import annotations

import io
import subprocess
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parents[3]

# 텍스트로 다루는 확장자만 검사한다. 바이너리/이미지/폰트는 대상이 아니다.
TEXT_SUFFIXES = {
    ".md", ".txt", ".json", ".yml", ".yaml", ".ps1", ".sh", ".py", ".java", ".gradle",
    ".ts", ".tsx", ".js", ".mjs", ".sql", ".xml", ".properties", ".csv", ".cmd", ".bat",
}

# 텍스트에서 정상적으로 쓰이는 제어문자.
ALLOWED = {0x09, 0x0A, 0x0D}


def tracked_text_files() -> list[Path]:
    """git 이 추적하는 파일만 본다. 빌드 산출물/캐시는 대상이 아니다."""
    done = subprocess.run(["git", "ls-files", "-z"], cwd=str(REPO_ROOT),
                          capture_output=True, timeout=180)
    if done.returncode != 0:
        return []
    names = done.stdout.decode("utf-8", errors="replace").split("\0")
    files = []
    for name in names:
        if not name:
            continue
        path = REPO_ROOT / name
        if path.suffix.lower() in TEXT_SUFFIXES and path.is_file():
            files.append(path)
    return files


class TextControlCharacterContractTest(unittest.TestCase):
    def test_no_tracked_text_file_contains_a_stray_control_character(self) -> None:
        files = tracked_text_files()
        self.assertTrue(files, "git 이 추적하는 텍스트 파일을 찾지 못했다.")
        offenders: list[str] = []
        for path in files:
            data = io.open(path, "rb").read()
            found = sorted({byte for byte in data if byte < 0x20 and byte not in ALLOWED})
            if found:
                relative = path.relative_to(REPO_ROOT).as_posix()
                offenders.append(f"{relative}: {[hex(b) for b in found]}")
        self.assertEqual(
            [], offenders,
            "텍스트 파일에 제어문자가 섞였다. heredoc/편집 경로가 이스케이프를 실제 문자로 "
            "치환했을 가능성이 높다: " + "; ".join(offenders[:20]))


if __name__ == "__main__":
    unittest.main()
