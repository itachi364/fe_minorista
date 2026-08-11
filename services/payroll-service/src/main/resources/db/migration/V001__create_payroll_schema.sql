create schema if not exists payroll;

create table if not exists payroll.payroll_settings (
    company_id uuid primary key,
    electronic_payroll_enabled boolean not null default false,
    provider_mode varchar(30) not null default 'MOCK',
    updated_at timestamptz not null default now()
);

create table if not exists payroll.worker (
    id uuid primary key,
    company_id uuid not null,
    identification_type_code smallint not null,
    identification_number varchar(40) not null,
    verification_digit smallint,
    full_name varchar(180) not null,
    worker_classification varchar(40) not null,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    unique (company_id, identification_type_code, identification_number)
);

create table if not exists payroll.daily_labor_payment (
    id uuid primary key,
    company_id uuid not null,
    worker_id uuid not null references payroll.worker(id),
    work_date date not null,
    activity_description varchar(300) not null,
    agreed_amount numeric(19,2) not null,
    paid_amount numeric(19,2) not null,
    payment_method_code varchar(40) not null,
    legal_notice_accepted boolean not null,
    notes varchar(500),
    created_at timestamptz not null default now()
);

create index if not exists ix_worker_company on payroll.worker(company_id);
create index if not exists ix_daily_labor_payment_company_date on payroll.daily_labor_payment(company_id, work_date desc);
