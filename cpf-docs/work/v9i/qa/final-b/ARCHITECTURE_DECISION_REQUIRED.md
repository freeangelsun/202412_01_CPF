# ARCHITECTURE DECISION REQUIRED

1. EDU-ADM PRODUCT_ADM/MERGE_EDU concrete handler physical retention policy.
   - non-executable metadata만 남길지
   - 실제 Product source reference로 치환할지
   - 승인된 Delete Manifest 후 제거할지
2. HIGH/CRITICAL Frontend mutation의 canonical consumer 방식.
   - 현재 QA gate는 generated API direct call을 요구한다.
   - Gate를 약화하지 말고, custom wrapper가 필요하면 동등 이상의 typed/generated contract로 중앙 Architecture 승인을 받아야 한다.
