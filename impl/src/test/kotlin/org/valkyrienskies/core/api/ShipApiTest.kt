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

    fun abstractShipSaver(ship: ServerShipCore) {
        ship.saveAttachment(3f)
    }

    fun abstractShipSaver2(ship: LoadedServerShipCore) {
        ship.setAttachment(5)
    }

    fun abstractShipUser(ship: ServerShipCore, checkInt: Boolean) {
        if (checkInt) Assertions.assertEquals(5, ship.getAttachment<Int>())
        Assertions.assertEquals(3f, ship.getAttachment<Float>())
    }
}
