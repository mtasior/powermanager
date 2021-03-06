package output

interface ChargingPowerController {
    /**
     * sets the new desired maximum charging power. Can calculate the most appropriate value.
     * This can be useful if the charging box has a defined minimum value.
     *
     * The value can also reach 0f. In this case it would be good to switch the box off. Including a hystheresis.
     * Value is >= 0.0
     */
    fun setNewChargingPowerKiloWatt(desiredPowerKiloWatt: Float)


    /**
     * return the current charging consumption in kiloWatt. This is definitely called before setting the first charging power.
     */
    fun getcurrentConsumptionkiloWatt(): Float


    /**
     * return the current CarState. Used for visualization in the App. Always called right AFTER getcurrentConsumptionkiloWatt()
     */
    fun getCurrentCarState(): CarState
}

enum class CarState { BOX_READY_NO_CAR, CAR_CHARGING, WAITING_FOR_CAR, CHARGING_ENDED_CAR_CONNECTED, UNKNOWN }