package org.valkyrienskies.core.api

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.valkyrienskies.core.VSRandomUtils
import org.valkyrienskies.core.api.ships.getAttachment
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.core.api.ships.setAttachment
import org.valkyrienskies.core.game.ships.ShipObjectServer

// Yes its a very simple test, but if somebody ever dares to break it we will know
class ShipApiTest {

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

    fun abstractShipSaver(ship: ServerShipInternal) {
        ship.saveAttachment(3f)
    }

    fun abstractShipSaver2(ship: LoadedServerShipInternal) {
        ship.setAttachment(5)
    }

    fun abstractShipUser(ship: ServerShipInternal, checkInt: Boolean) {
        if (checkInt) Assertions.assertEquals(5, ship.getAttachment<Int>())
        Assertions.assertEquals(3f, ship.getAttachment<Float>())
    }
}
