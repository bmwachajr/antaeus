package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random


class BillingServiceTest {
    val invoices = List(size = 7) { 
        Invoice(
                id = Random.nextInt(),
                customerId = Random.nextInt(),
                amount = Money(100.toBigDecimal(), Currency.USD),
                status = InvoiceStatus.PENDING
        )
    }

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoicesByStatus("pending") } returns invoices
        every { updateInvoiceStatus(any(), InvoiceStatus.PAID) } returns Unit
        every { updateInvoiceStatus(any(), InvoiceStatus.UNPAID) } returns Unit
    }

    @Test
    fun `it updates invoice status to PAID when an invoice is successfully charged`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } returns true }
        val billingService = BillingService(paymentProvider, dal)
        val results = billingService.billAll()
        
        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.PAID)
        }
    }

    @Test
    fun `it updates invoice status to UNPAID when an invoice is unsuccessfully charged`() {
        val aymentProvider = mockk<PaymentProvider> { every { charge(any()) } returns false }
        val billingService = BillingService(aymentProvider, dal)
        val results = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.UNPAID)
        }
    }

    @Test
    fun `will doesnt update invoice status if an NetworkException is encountered`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } throws NetworkException() }
        val billingService = BillingService(paymentProvider, dal)
        val result = billingService.billAll()

        verify(exactly = 0) {
            dal.updateInvoiceStatus(any(), any())
        }
    }

    @Test
    fun `updates invoice status to unpaid if CustomerNotFoundException is thrown`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } throws CustomerNotFoundException(1) }
        val billingService = BillingService(paymentProvider, dal)
        val result = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.UNPAID)
        }
    }

    @Test
    fun `updates invoice status to unpaid if CurrencyMismatchException is thrown`() {
        val paymentProvider = mockk<PaymentProvider> { every { charge(any()) } throws CurrencyMismatchException(1,1) }
        val billingService = BillingService(paymentProvider, dal)
        val result = billingService.billAll()

        verify(exactly = 7) {
            dal.updateInvoiceStatus(any(), InvoiceStatus.UNPAID)
        }
    }

}
