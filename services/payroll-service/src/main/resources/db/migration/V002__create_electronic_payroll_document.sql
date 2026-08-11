create table if not exists payroll.electronic_payroll_document (
    id uuid primary key,
    company_id uuid not null,
    daily_labor_payment_id uuid not null references payroll.daily_labor_payment(id),
    cune varchar(120) not null,
    status varchar(30) not null,
    provider_response varchar(500),
    created_at timestamptz not null default now(),
    unique (company_id, daily_labor_payment_id)
);

create index if not exists ix_electronic_payroll_document_company on payroll.electronic_payroll_document(company_id, created_at desc);
