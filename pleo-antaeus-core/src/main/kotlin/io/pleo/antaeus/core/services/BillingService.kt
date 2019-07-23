package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val dal: AntaeusDal
) {
   // TODO - Add code e.g. here
   fun billAll(): String {
       val pendingInvoices = dal.fetchInvoicesByStatus("pending")
       
       pendingInvoices.forEach {
           when(processPayment(it)) {
               true -> { dal.updateInvoiceStatus(it.id, InvoiceStatus.PAID, "Successfully Paid") }
               false -> { dal.updateInvoiceStatus(it.id, InvoiceStatus.UNPAID, "Customer Balance Low") }
           }
       }

       return("Invoices Billed")
   }

   fun processPayment(invoice: Invoice): Any {
       try {
            return paymentProvider.charge(invoice)
        }
        catch (e: CustomerNotFoundException) {
            logger.error(e) { CustomerNotFoundException(invoice.customerId) }
            return dal.updateInvoiceStatus(invoice.id, InvoiceStatus.UNPAID, "Customer Not Found")
        }
        catch (e: CurrencyMismatchException) {
            logger.error(e) { CurrencyMismatchException(invoice.id, invoice.customerId) }
            return dal.updateInvoiceStatus(invoice.id, InvoiceStatus.UNPAID, "Currency Mis-match")

        }
        catch (e: NetworkException) {
            logger.error(e) { NetworkException() }
            return dal.updateInvoiceStatus(invoice.id, InvoiceStatus.PENDING, "Network Error")
        } 
   }
}