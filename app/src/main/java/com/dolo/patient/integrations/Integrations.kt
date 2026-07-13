package com.dolo.patient.integrations

enum class IntegrationType {
    MAPS,
    PAYMENTS,
    SMS,
    PUSH_NOTIFICATIONS
}

enum class IntegrationMode {
    DISABLED,
    DEMO,
    LIVE
}

data class IntegrationCapability(
    val type: IntegrationType,
    val title: String,
    val description: String,
    val mode: IntegrationMode = IntegrationMode.DISABLED,
    val providerName: String? = null
)

sealed interface ProviderResult<out T> {
    data class Success<T>(val value: T) : ProviderResult<T>
    data class Unavailable(
        val code: String,
        val message: String
    ) : ProviderResult<Nothing>
}

data class PaymentOrder(
    val reference: String,
    val appointmentId: String,
    val amountPaise: Int,
    val currency: String = "INR"
)

data class DeliveryReceipt(
    val reference: String,
    val accepted: Boolean
)

interface MapsProvider {
    fun navigationUri(
        latitude: Double?,
        longitude: Double?,
        clinicLabel: String
    ): ProviderResult<String>
}

interface PaymentProvider {
    fun createOrder(
        appointmentId: String,
        amountPaise: Int
    ): ProviderResult<PaymentOrder>

    fun verifyPayment(reference: String): ProviderResult<Boolean>
}

interface SmsProvider {
    fun sendOtp(mobileNumber: String): ProviderResult<DeliveryReceipt>

    fun sendAppointmentReminder(
        mobileNumber: String,
        appointmentId: String
    ): ProviderResult<DeliveryReceipt>
}

interface PushProvider {
    fun registerDevice(deviceToken: String): ProviderResult<DeliveryReceipt>

    fun sendQueueAlert(
        deviceToken: String,
        appointmentId: String,
        patientsAhead: Int
    ): ProviderResult<DeliveryReceipt>
}

private fun unavailable(type: IntegrationType): ProviderResult.Unavailable =
    ProviderResult.Unavailable(
        code = type.name + "_DISABLED",
        message = "A live provider has not been configured."
    )

object DisabledMapsProvider : MapsProvider {
    override fun navigationUri(
        latitude: Double?,
        longitude: Double?,
        clinicLabel: String
    ): ProviderResult<String> = unavailable(IntegrationType.MAPS)
}

object DisabledPaymentProvider : PaymentProvider {
    override fun createOrder(
        appointmentId: String,
        amountPaise: Int
    ): ProviderResult<PaymentOrder> = unavailable(IntegrationType.PAYMENTS)

    override fun verifyPayment(reference: String): ProviderResult<Boolean> =
        unavailable(IntegrationType.PAYMENTS)
}

object DisabledSmsProvider : SmsProvider {
    override fun sendOtp(mobileNumber: String): ProviderResult<DeliveryReceipt> =
        unavailable(IntegrationType.SMS)

    override fun sendAppointmentReminder(
        mobileNumber: String,
        appointmentId: String
    ): ProviderResult<DeliveryReceipt> = unavailable(IntegrationType.SMS)
}

object DisabledPushProvider : PushProvider {
    override fun registerDevice(deviceToken: String): ProviderResult<DeliveryReceipt> =
        unavailable(IntegrationType.PUSH_NOTIFICATIONS)

    override fun sendQueueAlert(
        deviceToken: String,
        appointmentId: String,
        patientsAhead: Int
    ): ProviderResult<DeliveryReceipt> =
        unavailable(IntegrationType.PUSH_NOTIFICATIONS)
}

object IntegrationRegistry {
    val patientCapabilities: List<IntegrationCapability> = listOf(
        IntegrationCapability(
            type = IntegrationType.MAPS,
            title = "Maps and navigation",
            description = "Clinic coordinates, distance and turn-by-turn navigation."
        ),
        IntegrationCapability(
            type = IntegrationType.PAYMENTS,
            title = "Secure payments",
            description = "Consultation charges, verification and future refunds."
        ),
        IntegrationCapability(
            type = IntegrationType.SMS,
            title = "SMS messages",
            description = "OTP, appointment reminders and queue alerts."
        ),
        IntegrationCapability(
            type = IntegrationType.PUSH_NOTIFICATIONS,
            title = "Push notifications",
            description = "Background turn alerts and appointment updates."
        )
    )

    fun capability(type: IntegrationType): IntegrationCapability =
        patientCapabilities.first { capability -> capability.type == type }
}
