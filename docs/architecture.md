&#x20;Users / actors



Customer — registers, logs in, manages accounts, moves money, manages beneficiaries, views history

Admin — oversees customers/accounts/transactions, can freeze accounts, views audit trails

System/internal actors — services calling each other, the notification dispatcher, the scheduler (if we add one later for statements)



Core business capabilities



Identity \& access (register, login, JWT, roles)

Customer profile management

Account lifecycle (open, view, freeze/unfreeze, close)

Money movement (deposit, withdraw, internal transfer, beneficiary payment)

Transaction history \& statements

Beneficiary management

Notifications (simulated)

Admin oversight \& audit



Data ownership (bounded contexts)



Domain	Owns

Auth	credentials, roles, JWT issuance

Customer	profile data (name, contact, KYC-lite fields)

Banking/Account	accounts, balances, freeze status

Payment	beneficiaries, payment requests, idempotency keys

Transaction	transaction ledger, statements, audit trail

Notification	notification log (simulated sends)



Why split this way: Auth is a security boundary — it should never mix concerns with customer data. Banking/Account and Transaction look similar but aren't: Account is the current state (balance, status) that must be strongly consistent and transactional; Transaction is the historical ledger that's append-only and read-heavy — different consistency and scaling needs, so they get separate services and databases. Payment is deliberately separate from Account because payments involve external-facing concerns (idempotency, beneficiary validation, retries) that shouldn't live inside the core account/balance logic.



Dependencies between services



Payment Service → needs to check account existence/balance → calls Banking Service (sync, REST/Feign)

Every money-movement operation → emits an event → Transaction Service records it, Notification Service reacts to it (async)

Admin operations → read across Customer, Banking, Transaction (via gateway/aggregation, not direct DB access)



Security boundaries: Only Auth issues/validates JWTs.



Every other service trusts a validated JWT passed through the gateway (or re-validates it) — no service does its own username/password checks.



&#x20;                        ┌───────────────────┐

&#x20;                        │  React Frontend    │

&#x20;                        └─────────┬──────────┘

&#x20;                                  │

&#x20;                                  ▼

&#x20;                        ┌───────────────────┐

&#x20;                        │   API Gateway      │  (Spring Cloud Gateway)

&#x20;                        └─────────┬──────────┘

&#x20;                                  │

&#x20;       ┌───────────┬─────────────┼──────────────┬───────────────┐

&#x20;       ▼            ▼             ▼              ▼               ▼

&#x20;    Auth Svc   Customer Svc   Banking Svc    Payment Svc     Transaction Svc

&#x20;       │            │             │              │               │

&#x20;       ▼            ▼             ▼              ▼               ▼

&#x20;  Auth DB      Customer DB    Banking DB     Payment DB      Transaction DB



&#x20;    Payment Svc ──(REST/Feign, sync)──► Banking Svc  (check balance/account)



&#x20;    Banking Svc ─┐

&#x20;    Payment Svc ─┼──(events)──► Kafka ──► Transaction Svc (records ledger entry)

&#x20;                 │                    └──► Notification Svc (simulated email)

Step 3 — Why this shape

Auth as its own service: isolates the one place secrets/passwords live; every other service is stateless w.r.t. identity.

Customer vs Auth split: Auth owns "can this person log in," Customer owns "who is this person." Classic separation so profile changes never touch credential logic.

Banking (accounts) vs Transaction (ledger) split: Account needs strong consistency + locking for balance updates; Transaction is an append-only audit log that can be eventually consistent and optimized for read/filter/pagination. Different data-access patterns justify different services.

Payment as its own service: payments need idempotency keys, beneficiary validation, and retry-safety — different concerns from "does this account have money in it." Keeping it separate means a payment retry storm can't directly hammer the account-balance code path.

Notification decoupled via events: nothing should block on "did the email send." Async by design from day one, even while simulated.

Database-per-service: no service can silently depend on another's schema. Forces you to design real APIs/events instead of shortcutting with a join — which is exactly the skill this portfolio project is meant to prove you have.

