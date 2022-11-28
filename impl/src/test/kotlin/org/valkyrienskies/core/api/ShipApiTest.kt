package org.valkyrienskies.core.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.valkyrienskies.core.VSRandomUtils
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.getAttachment
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.core.api.ships.setAttachment
import org.valkyrienskies.core.game.serialization.DaggerServerShipDataConverterComponent
import org.valkyrienskies.core.game.ships.ShipObjectServer

// Yes its a very simple test, but if somebody ever dares to break it we will know
internal class ShipApiTest {

    @Test
    fun testShipDataAbstraction() {
        val shipData = VSRandomUtils.randomShipData()

        abstractShipSaver(shipData)
        // abstractShipSaver2(shipData) does not compile (wich is good)
        abstractShipUser(shipData, false)
    }

    @Test
    fun testShipObjectAbstraction() {
        val shipObject = ShipObjectServer(VSRandomUtils.randomShipData())

        abstractShipSaver(shipObject)
        abstractShipSaver2(shipObject)
        abstractShipUser(shipObject, true)
    }

    @Test
    fun testAttachmentInterfaces() {
        val shipData = VSRandomUtils.randomShipData()
        val user = TestShipUser()

        shipData.saveAttachment(user)

        Assertions.assertEquals(user.ship, shipData)
        Assertions.assertEquals(user, shipData.getAttachment(TestShipUser::class.java))

        val comp = DaggerServerShipDataConverterComponent.create()

        val shipDataSerialized = comp.dtoMapper().writeValueAsString(comp.converter().convertToDto(shipData))
        val shipDataDeserialized = comp.converter().convertToModel(comp.dtoMapper().readValue(shipDataSerialized))

        Assertions.assertNotNull(shipData.getAttachment(TestShipUser::class.java))
        Assertions.assertEquals(shipData.getAttachment(TestShipUser::class.java)!!.ship, shipDataDeserialized)
    }

    fun abstractShipSaver(ship: ServerShip) {
        ship.saveAttachment(3f)
    }

    fun abstractShipSaver2(ship: LoadedServerShip) {
        ship.setAttachment(5)
    }

    fun abstractShipUser(ship: ServerShip, checkInt: Boolean) {
        if (checkInt) Assertions.assertEquals(5, ship.getAttachment<Int>())
        Assertions.assertEquals(3f, ship.getAttachment<Float>())
    }
}

internal class TestShipUser : ServerShipUser {
    @JsonIgnore
    override var ship: ServerShip? = null
}
