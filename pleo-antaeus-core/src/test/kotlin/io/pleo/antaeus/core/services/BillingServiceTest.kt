package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.InvoiceService

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random


class BillingServiceTest {
    val invoices = List(size = 7) { 
        Invoice(
                id = Random.nextInt(),
                customerId = Random.nextInt(),
                amount = Money(100.toBigDecimal(), Currency.USD),
                status = InvoiceStatus.PENDING,
                comments = "Successfully Created"
        )
    }

    val paidInvoice = Invoice(
            id = Random.nextInt(),
            customerId = Random.nextInt(),
            amount = Money(100.toBigDecimal(), Currency.USD),
            status = InvoiceStatus.PAID,
            comments = "Successfully Created"
    )

    private val invoiceService = mockk<InvoiceService> {}

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoicesByStatus("pending") } returns invoices
        every { updateInvoiceStatus(any(), any(), any()) } returns Unit
    }

    @Test
    fun `it bills an unpaid invoice`() {
        val invoiceService = mockk<InvoiceService> { every { fetch(any()) } returns invoices[1] }
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } returns true }
        val billingService = BillingService(paymentProvider, invoiceService, dal)
        val results = billingService.bill(1)

        verify(exactly = 1) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.PAID, "Successfully Paid")
        }
    }

    @Test
    fun `it doesnt bill a paid invoice`() {
        val invoiceService = mockk<InvoiceService> { every { fetch(any()) } returns paidInvoice }
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } returns true }
        val billingService = BillingService(paymentProvider, invoiceService, dal)
        val results = billingService.bill(1)

        verify(exactly = 0) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.PAID, "Successfully Paid")
        }
    }

    @Test
    fun `it updates invoice status to PAID when an invoice is successfully charged`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } returns true }
        val billingService = BillingService(paymentProvider, invoiceService, dal)
        val results = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.PAID, "Successfully Paid")
        }
    }

    @Test
    fun `it updates invoice status to UNPAID when an invoice is unsuccessfully charged`() {
        val aymentProvider = mockk<PaymentProvider> { every { charge(any()) } returns false }
        val billingService = BillingService(aymentProvider, invoiceService, dal)
        val results = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.UNPAID, "Customer Balance Low")
        }
    }

    @Test
    fun `doesnt update invoice status if an NetworkException is encountered`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } throws NetworkException() }
        val billingService = BillingService(paymentProvider, invoiceService, dal)
        val result = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.PENDING, "Network Error")
        }
    }

    @Test
    fun `updates invoice status to unpaid if CustomerNotFoundException is thrown`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } throws CustomerNotFoundException(1) }
        val billingService = BillingService(paymentProvider, invoiceService, dal)
        val result = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.UNPAID, "Customer Not Found")
        }
    }

    @Test
    fun `updates invoice status to unpaid if CurrencyMismatchException is thrown`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } throws CurrencyMismatchException(1,1) }
        val billingService = BillingService(paymentProvider, invoiceService, dal)
        val result = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.UNPAID, "Currency Mis-match")
        }
    }

}
