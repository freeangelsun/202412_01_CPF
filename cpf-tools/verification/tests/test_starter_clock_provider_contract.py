from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[3]
STARTERS = ROOT / "cpf-starters"


def test_generic_optional_clock_consumers_are_safe_with_multiple_owner_clocks():
    unsafe = []
    for source in STARTERS.rglob("src/main/java/**/*.java"):
        text = source.read_text(encoding="utf-8")
        if not re.search(r"ObjectProvider\s*<\s*(?:java\.time\.)?Clock\s*>", text):
            continue
        if re.search(r"(?:clockProvider|clocks)\.getIfAvailable\s*\(\s*(?:Clock::systemUTC)?\s*\)", text):
            unsafe.append(source.relative_to(ROOT).as_posix())

    assert unsafe == [], (
        "Generic ObjectProvider<Clock> consumers must use getIfUnique(Clock::systemUTC) "
        f"because composed CPF runtimes contain multiple owner-scoped Clock beans: {unsafe}"
    )
