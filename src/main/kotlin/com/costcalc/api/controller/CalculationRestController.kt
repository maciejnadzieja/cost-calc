import com.costcalc.api.config.CurrentUser
import com.costcalc.api.dto.CalculationRequest
import com.costcalc.api.dto.CalculationResponse
import com.costcalc.api.exception.ForbiddenException
import com.costcalc.api.mapper.toResponse
import com.costcalc.api.model.Calculation
import com.costcalc.api.model.CalculationId
import com.costcalc.api.model.User
import com.costcalc.api.service.CalculationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/calculations")
@PreAuthorize("isAuthenticated()")
class CalculationRestController(
    private val calculationService: CalculationService,
) {
    @GetMapping("/currencies")
    fun getSupportedCurrencies(): List<String> = listOf("EUR", "PLN", "USD")

    @GetMapping
    fun getCalculations(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @CurrentUser user: User,
    ): PagedModel<CalculationResponse> {
        val pageable = PageRequest.of(page, size)
        return PagedModel(calculationService.search(user.id, query, pageable).map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: CalculationId,
        @CurrentUser user: User,
    ): ResponseEntity<CalculationResponse> {
        val calculation = calculationService.findById(id)

        if (calculation.userId != user.id) {
            throw ForbiddenException("User does not have permission to view this calculation")
        }

        return ResponseEntity.ok(calculation.toResponse())
    }

    @PostMapping
    fun create(
        @RequestBody calculationRequest: CalculationRequest,
        @CurrentUser user: User,
    ): ResponseEntity<CalculationResponse> {
        val calculation =
            Calculation(
                id = UUID.randomUUID(),
                name = calculationRequest.name,
                description = calculationRequest.description,
                stages = calculationRequest.stages,
                currency = calculationRequest.currency,
                userId = user.id,
            )
        val createdCalculation = calculationService.create(calculation)
        return ResponseEntity.ok(createdCalculation.toResponse())
    }

    @PostMapping("/{id}/duplicate")
    fun duplicate(
        @PathVariable id: CalculationId,
        @CurrentUser user: User,
    ): ResponseEntity<CalculationResponse> {
        val duplicatedCalculation = calculationService.duplicate(id, user.id)
        return ResponseEntity.ok(duplicatedCalculation.toResponse())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: CalculationId,
        @RequestBody calculationRequest: CalculationRequest,
        @CurrentUser user: User,
    ): ResponseEntity<CalculationResponse> {
        val calculation =
            Calculation(
                id = id,
                name = calculationRequest.name,
                description = calculationRequest.description,
                stages = calculationRequest.stages,
                currency = calculationRequest.currency,
                userId = user.id,
            )
        val updatedCalculation = calculationService.update(id, calculation)
        return ResponseEntity.ok(updatedCalculation.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: CalculationId,
        @CurrentUser user: User,
    ): ResponseEntity<Void> {
        calculationService.delete(id, user.id)
        return ResponseEntity.noContent().build()
    }
}
