package me.dio.credit.application.system.service.impl

import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.ICreditService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.*

@Service
class CreditService(
  private val creditRepository: CreditRepository,
  private val customerService: CustomerService
) : ICreditService {
  override fun save(credit: Credit): Credit {
    this.validDayFirstInstallment(credit.dayFirstInstallment)
    credit.apply {
      customer = customerService.findById(credit.customer?.id!!)
    }
    return this.creditRepository.save(credit)
  }

  override fun findAllByCustomer(customerId: Long): List<Credit> {
    try {
        val customer = customerService.findById(customerId)
    } catch (e: RuntimeException) {
      throw BusinessException("Customer doesn't exists")
    }
    return this.creditRepository.findAllByCustomerId(customerId)
  }
  override fun findByCreditCode(customerId: Long, creditCode: UUID): Credit {
    val credit: Credit = (this.creditRepository.findByCreditCode(creditCode)
      ?: throw BusinessException("Creditcode $creditCode not found"))
    return if (credit.customer?.id == customerId) credit
    else throw IllegalArgumentException("Contact admin")
    /*if (credit.customer?.id == customerId) {
      return credit
    } else {
      throw RuntimeException("Contact admin")
    }*/
  }

  private fun validDayFirstInstallment(dayFirstOfInstallment: LocalDate): Boolean {
    return if (dayFirstOfInstallment.isBefore(LocalDate.now().plusMonths(2L))) true
    else throw BusinessException("The Date must be 3 months later")
  }
}

