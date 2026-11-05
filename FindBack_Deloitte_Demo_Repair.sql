/*
 FindBack Deloitte demo repair
 Run this ONLY after the original seed script stopped at:
 ERROR 1146 ... findback_claim_db.fb_demo_item_map
 The item rows and initial item history were already committed.
 This script inserts ONLY missing claims, claim status history and return.
 It avoids duplicate demo claims if accidentally run more than once.
*/

USE findback_claim_db;
START TRANSACTION;

-- Pending claim for the found blue water bottle.
INSERT INTO claims
(organization_id, item_id, user_id, claim_details, status, reviewed_by)
SELECT 1, i.item_id, 4,
       'The bottle has my name sticker and a small scratch near the lid.',
       'PENDING', NULL
FROM findback_item_db.items i
WHERE i.organization_id=1
  AND i.item_name='Blue Water Bottle'
  AND i.item_type='FOUND'
  AND i.description='Blue insulated bottle found on a cafeteria table.'
  AND NOT EXISTS (
      SELECT 1 FROM claims c
      WHERE c.organization_id=1
        AND c.claim_details='The bottle has my name sticker and a small scratch near the lid.'
  )
ORDER BY i.item_id DESC
LIMIT 1;

-- Approved claim for the found black wallet.
INSERT INTO claims
(organization_id, item_id, user_id, claim_details, status, reviewed_by)
SELECT 1, i.item_id, 2,
       'This wallet contains my ID card and personal cards. I can identify the contents.',
       'APPROVED', 1
FROM findback_item_db.items i
WHERE i.organization_id=1
  AND i.item_name='Black Wallet'
  AND i.item_type='FOUND'
  AND i.description='Black leather wallet found near the library entrance.'
  AND NOT EXISTS (
      SELECT 1 FROM claims c
      WHERE c.organization_id=1
        AND c.claim_details='This wallet contains my ID card and personal cards. I can identify the contents.'
  )
ORDER BY i.item_id DESC
LIMIT 1;

-- Rejected claim for headphones.
INSERT INTO claims
(organization_id, item_id, user_id, claim_details, status, reviewed_by)
SELECT 1, i.item_id, 2,
       'I believe these headphones are mine; I can provide the purchase details.',
       'REJECTED', 1
FROM findback_item_db.items i
WHERE i.organization_id=1
  AND i.item_name='Wireless Headphones'
  AND i.item_type='FOUND'
  AND i.description='White wireless headphones in a compact charging case.'
  AND NOT EXISTS (
      SELECT 1 FROM claims c
      WHERE c.organization_id=1
        AND c.claim_details='I believe these headphones are mine; I can provide the purchase details.'
  )
ORDER BY i.item_id DESC
LIMIT 1;

-- Collected claim for the smart watch.
INSERT INTO claims
(organization_id, item_id, user_id, claim_details, status, reviewed_by)
SELECT 1, i.item_id, 3,
       'The watch has a distinctive scratch on the left side and is paired with my phone.',
       'COLLECTED', 1
FROM findback_item_db.items i
WHERE i.organization_id=1
  AND i.item_name='Smart Watch'
  AND i.item_type='FOUND'
  AND i.description='Black smart watch with a dark silicone strap.'
  AND NOT EXISTS (
      SELECT 1 FROM claims c
      WHERE c.organization_id=1
        AND c.claim_details='The watch has a distinctive scratch on the left side and is paired with my phone.'
  )
ORDER BY i.item_id DESC
LIMIT 1;

-- Set item statuses to match the claim states.
UPDATE findback_item_db.items i
JOIN claims c ON c.item_id=i.item_id AND c.organization_id=i.organization_id
SET i.status = CASE c.status
    WHEN 'PENDING' THEN 'CLAIM_PENDING'
    WHEN 'APPROVED' THEN 'CLAIM_APPROVED'
    WHEN 'REJECTED' THEN 'AVAILABLE'
    WHEN 'COLLECTED' THEN 'RETURNED'
    ELSE i.status
END
WHERE c.organization_id=1
  AND c.claim_details IN (
    'The bottle has my name sticker and a small scratch near the lid.',
    'This wallet contains my ID card and personal cards. I can identify the contents.',
    'I believe these headphones are mine; I can provide the purchase details.',
    'The watch has a distinctive scratch on the left side and is paired with my phone.'
  );

-- Insert return row for the collected watch claim only, if not already present.
INSERT INTO returns
(organization_id, claim_id, item_id, user_id, verified_by, return_date)
SELECT c.organization_id, c.claim_id, c.item_id, c.user_id, 1, NOW()
FROM claims c
WHERE c.organization_id=1
  AND c.status='COLLECTED'
  AND c.claim_details='The watch has a distinctive scratch on the left side and is paired with my phone.'
  AND NOT EXISTS (
      SELECT 1 FROM returns r WHERE r.claim_id=c.claim_id
  );

COMMIT;

-- Add missing status-history transitions once, based on current status.
USE findback_item_db;

INSERT INTO item_status_history (item_id, old_status, new_status, updated_by)
SELECT i.item_id, 'AVAILABLE', i.status,
       CASE WHEN c.status='PENDING' THEN c.user_id ELSE 1 END
FROM items i
JOIN findback_claim_db.claims c
  ON c.item_id=i.item_id AND c.organization_id=i.organization_id
WHERE i.organization_id=1
  AND c.claim_details IN (
    'The bottle has my name sticker and a small scratch near the lid.',
    'This wallet contains my ID card and personal cards. I can identify the contents.',
    'I believe these headphones are mine; I can provide the purchase details.',
    'The watch has a distinctive scratch on the left side and is paired with my phone.'
  )
  AND NOT EXISTS (
      SELECT 1 FROM item_status_history h
      WHERE h.item_id=i.item_id AND h.new_status=i.status
  );

-- Final verification.
SELECT status, COUNT(*) AS total
FROM findback_claim_db.claims
WHERE organization_id=1
GROUP BY status
ORDER BY status;

SELECT return_id, claim_id, item_id, user_id, verified_by, return_date
FROM findback_claim_db.returns
WHERE organization_id=1
ORDER BY return_id DESC;
