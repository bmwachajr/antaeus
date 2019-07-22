package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val dal: AntaeusDal
) {
   // TODO - Add code e.g. here
   fun billAll(): List<Invoice> {
       val pendingInvoices = dal.fetchInvoicesByStatus("pending")
       
       pendingInvoices.forEach {
           val chargeStatus = true
           if (ç == true) {
               dal.updateInvoiceStatus(it.id)
           }
       }

       return pendingInvoices
   }

   fun processPayment(invoice: Invoice): Boolean {
       try {
            return paymentProvider.charge(invoice)
        }
        catch (e: CustomerNotFoundException) {
            throw CustomerNotFoundException(invoice.customerId)
        }
        catch (e: CurrencyMismatchException) {
            throw CurrencyMismatchException(invoice.id, invoice.customerId)
        }
        catch (e: NetworkException) {
            throw NetworkException()
        } 
        finally {
            return false
        }
   }
}