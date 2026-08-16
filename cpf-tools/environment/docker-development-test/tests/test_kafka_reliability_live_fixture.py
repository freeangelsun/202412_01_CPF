from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
RUNNER = ROOT / "run-kafka-reliability-live.ps1"


class KafkaReliabilityLiveFixtureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.text = RUNNER.read_text(encoding="utf-8")

    def test_git_independent_identity_and_owned_restart_contract(self):
        self.assertIn("SourceIdentity = $env:CPF_SOURCE_SHA", self.text)
        self.assertIn("GIT_INDEPENDENT_CONTENT_SHA1", self.text)
        self.assertNotIn("git rev-parse", self.text)
        self.assertNotIn("git status", self.text)
        self.assertIn("RestartBrokerIfOwned", self.text)
        self.assertIn("docker", self.text)
        self.assertIn("restart-persistence", self.text)

    def test_topic_is_unique_and_deleted(self):
        self.assertIn("cpf-local-validation-", self.text)
        self.assertIn("--delete", self.text)
        self.assertIn("--from-beginning", self.text)
        self.assertIn("before-restart-", self.text)
        self.assertIn("after-restart-", self.text)


if __name__ == "__main__":
    unittest.main()
