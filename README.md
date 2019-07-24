## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will pay those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

### Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|
|       Packages containing the main() application. 
|       This is where all the dependencies are instantiated.
|
├── pleo-antaeus-core
|
|       This is where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|
|       Module interfacing with the database. Contains the models, mappings and access layer.
|
├── pleo-antaeus-models
|
|       Definition of models used throughout the application.
|
├── pleo-antaeus-rest
|
|        Entry point for REST API. This is where the routes are defined.
└──
```



## Thought Process
The challenge is to create the logic to pay invoices on the first of the month. The principles that I abided by are:
1. Each Pleo customer is charged using their pleo card.
2. Invoices can have 3 statuses `paid` `unpaid` and `pending`
3. Invoices that fail to be paid get a status `unpaid` and a comment about why.
3. Unpaid Invoices can be manually charged using the api.

# In completing this challenge, I implemented 3 features:
1. [FetchInvoiceByStatus](https://github.com/bmwachajr/antaeus/pull/1): AS the name suggeste, this feature enables us to fetch invoices by their status using the path `/v1/invoices/status/{:status}`.
  `/v1/invoices/status/{:status}` - Returns a list of linvoices with the status `{:status}`.

2. [Billing Service Endpoint](https://github.com/bmwachajr/antaeus/pull/2): This feature is enables us to bill the pending invoices `/v1/billing`. 
Implemented as an api path+endpoint so that it can be triggered. There is no mention of automatically/manually triggering invoice billing on the first of the month. This approach caters for both scenarios. Manually and/or automatically(leveraging a cron).

3. [Bill an invoice](https://github.com/bmwachajr/antaeus/pull/4): It's a given that some invoices will fail to be paid either because of a low customer balance or an exception occurring. These Invoices can later on be manually charged using this feature `/v1/billing/{:invoice_id}`. This feature included extending the Invoices Model to include a new flied `comments`. Incase an invoice is not charges successfully, Its statius is set to unpaid as on the 1st on a month and a comment is added about why it was unpaid.

# Testing
To test this out I crested [BillingServiceTest](https://github.com/bmwachajr/antaeus/pull/3)

# Time spent
I am new to kotlin. I spent a `week` leveling up on kotlin. An experimenting with the apps javalin framework.
The actually challenge took me approx `3 hours a day` , `3 days`. Total approximately `9hours`

[Project Board](https://github.com/bmwachajr/antaeus/projects/1)

## How to run
```
./docker-start.sh
```

## Libraries currently in use
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
