from __future__ import annotations


def test_execution_suffix_may_move_to_a_later_phase() -> None:
    # execution_order's suffix preserves the Requirement sequence, not a phase-local counter.
    rows = [
        ("00-00010075", "CPF-FR-010075"),
        ("00-00010077", "CPF-FR-010077"),
        ("01-00010076", "CPF-FR-010076"),
    ]
    numeric = [(int(order[:2]), int(order[3:])) for order, _ in rows]
    assert numeric == sorted(numeric)
    assert {rid for _, rid in rows} == {
        "CPF-FR-010075", "CPF-FR-010076", "CPF-FR-010077"
    }
