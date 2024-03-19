package project.planItAPI.http.controllers

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import project.planItAPI.http.PathTemplates
import project.planItAPI.http.model.user.UserRegisterInputModel
import project.planItAPI.services.UsersService

@RestController
@RequestMapping(PathTemplates.PREFIX)
class UsersController(
    private val usersService: UsersService
) {
    @PostMapping(PathTemplates.REGISTER)
    fun register(@RequestBody inputData: UserRegisterInputModel) {

    }
}