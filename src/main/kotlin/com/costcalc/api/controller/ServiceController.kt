package com.costcalc.api.controller

import com.costcalc.api.dto.ServiceForm
import com.costcalc.api.model.Service
import com.costcalc.api.model.ServiceId
import com.costcalc.api.model.User
import com.costcalc.api.repository.ServiceRepository
import com.costcalc.api.repository.UserSettingsRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/services")
class ServiceController(
    private val serviceRepository: ServiceRepository,
    private val userSettingsRepository: UserSettingsRepository,
) {
    @GetMapping
    fun listServices(
        model: Model,
        user: User,
    ): String {
        val services = serviceRepository.findByUserId(user.id)
        val userSettings =
            userSettingsRepository.findById(user.id).orElseThrow {
                RuntimeException("User Not Found")
            }

        model.addAttribute("services", services)
        model.addAttribute("currency", userSettings?.currency ?: "USD")
        return "services/list"
    }

    @GetMapping("/new")
    fun newService(model: Model): String {
        model.addAttribute("service", ServiceForm())
        return "services/form"
    }

    @PostMapping
    fun updateService(
        @ModelAttribute serviceForm: ServiceForm,
        user: User,
    ): String {
        val service = serviceForm.toService(user.id)
        serviceRepository.save(service)
        return "redirect:/services/${service.id}/edit"
    }

    @GetMapping("/{id}/edit")
    fun editService(
        @PathVariable id: ServiceId,
        model: Model,
        user: User,
    ): String {
        val service = getServiceForUser(id, user)
        val userSettings =
            userSettingsRepository.findById(user.id).orElseThrow {
                RuntimeException("User Not Found")
            }

        model.addAttribute("service", ServiceForm.fromService(service))
        model.addAttribute("currency", userSettings?.currency ?: "USD")
        return "services/form"
    }

    @PostMapping("/{id}/delete")
    fun deleteService(
        @PathVariable id: ServiceId,
        user: User,
    ): String {
        getServiceForUser(id, user)
        serviceRepository.deleteById(id)
        return "redirect:/services"
    }

    internal fun getServiceForUser(
        id: ServiceId,
        user: User,
    ): Service {
        val service = serviceRepository.findById(id).orElseThrow()
        if (service.userId != user.id) {
            throw IllegalStateException("Not authorized to access this service")
        }
        return service
    }
}
