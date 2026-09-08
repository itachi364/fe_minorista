CREATE TABLE IF NOT EXISTS thirdparty.third_party_ciiu (
    third_party_id UUID NOT NULL,
    ciiu_code VARCHAR(10) NOT NULL,
    PRIMARY KEY (third_party_id, ciiu_code),
    CONSTRAINT fk_third_party_ciiu_third_party
        FOREIGN KEY (third_party_id) REFERENCES thirdparty.third_party (id) ON DELETE CASCADE
);

INSERT INTO thirdparty.third_party_ciiu (third_party_id, ciiu_code)
SELECT id, trim(ciiu_code)
FROM thirdparty.third_party
WHERE ciiu_code IS NOT NULL AND trim(ciiu_code) <> ''
ON CONFLICT (third_party_id, ciiu_code) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_third_party_ciiu_code
    ON thirdparty.third_party_ciiu (ciiu_code);
