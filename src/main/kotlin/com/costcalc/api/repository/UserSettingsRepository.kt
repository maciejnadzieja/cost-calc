package com.costcalc.api.repository

import com.costcalc.api.model.UserId
import com.costcalc.api.model.UserSettings
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSettingsRepository : MongoRepository<UserSettings, UserId>
