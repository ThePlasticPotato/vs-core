package org.valkyrienskies.core.api

import com.fasterxml.jackson.annotation.JsonIgnore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.valkyrienskies.core.VSRandomUtils
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.game.ships.ShipObjectServer
import org.valkyrienskies.core.util.serialization.VSJacksonUtil

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

        val shipDataSerialized = VSJacksonUtil.defaultMapper.writeValueAsBytes(shipData)
        val shipDataDeserialized = VSJacksonUtil.defaultMapper.readValue(shipDataSerialized, ShipData::class.java)

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
