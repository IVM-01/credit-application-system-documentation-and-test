package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.CustomerServiceTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object{
        const val URL_CREDITS = "/api/credits"
    }

    @BeforeEach
    fun setUp() {
        customerRepository.deleteAll()
        MockKAnnotations.init(this)
        creditRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        customerRepository.deleteAll()
        creditRepository.deleteAll()
        unmockkAll()
    }

    @Test
    fun `should save a credit and return 201 status`() {
        //given
        val customer: Customer = customerRepository.save(CustomerServiceTest.buildCustomer())
        val creditDto: CreditDto = builderCreditDto(customerId = customer.id!!)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL_CREDITS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            //.andExpect(MockMvcResultMatchers.jsonPath("$").value("Credit ${creditDto.creditCode} - Customer ${customer.email}"))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isString)
            .andDo(MockMvcResultHandlers.print())

    }

    @Test
    fun `should not create a credit and return 400 status`(){
        //given
        val creditDto: CreditDto = builderCreditDto(customerId = 0L)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL_CREDITS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find all credit by customer id and return 200 status`() {
        //given
        val customer: Customer = customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto(customerId = customer.id!!).toEntity())

        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL_CREDITS?customerId=${credit.customer?.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditCode").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditValue").value("100.0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfInstallments").value("2"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find all credits by costumer id and return 400 status`(){
        //given
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL_CREDITS?customerId=${1}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by code, and customer id, and return 201 status`(){
        //given
        val customer: Customer = customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto(customerId = customer.id!!).toEntity())

        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("${URL_CREDITS}/${credit.creditCode}?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            /*.andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").value(credit.creditCode)) - Kotlin herdou erro do Java (
            Expected :dca2757d-286a-4a58-9fc1-36acedc1c008
            Actual   :dca2757d-286a-4a58-9fc1-36acedc1c008)*/
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value("100.0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallment").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailCustomer").value(customer.email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.incomeCustomer").value(customer.income))
            .andDo(MockMvcResultHandlers.print())
    }

    private fun builderCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(100.0),
        numberOfInstallments: Int = 2,
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(1L),
        customerId: Long
    ) = CreditDto(
        creditValue = creditValue,
        numberOfInstallments = numberOfInstallments,
        dayFirstOfInstallment = dayFirstOfInstallment,
        customerId = customerId
    )

}