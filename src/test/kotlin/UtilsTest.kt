import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Test
import ru.inforion.ttgen.utils.Utilities
import ru.inforion.ttgen.utils.Utilities.batch

class UtilsTest {
    @Test fun testStatus() {
        val active = Utilities.STATUS_ACTIVE.toInt()
        val old = Utilities.STATUS_OLD.toInt()
        val deleted = Utilities.STATUS_DELETED.toInt()

        Assert.assertEquals(active, 0)
        Assert.assertEquals(old, 1)
        Assert.assertEquals(deleted, 2)
    }

    @Test fun bordersTest() {
        Assert.assertEquals(Utilities.AUTO_BWD_GEN, 180)
        Assert.assertEquals(Utilities.AUTO_FWD_GEN, 180)
        Assert.assertEquals(Utilities.SHIP_BWD_GEN, 365)
        Assert.assertEquals(Utilities.SHIP_FWD_GEN, 365)
        Assert.assertEquals(Utilities.RAIL_BWD_GEN, 60)
        Assert.assertEquals(Utilities.RAIL_FWD_GEN, 60)
    }

    @Test fun testConvertToListOfGroupsWithoutConsumingGroup() {
        val listOfGroups = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).asSequence().batch(2).toList()
        Assert.assertEquals(5, listOfGroups.size)
        Assert.assertEquals(listOf(1,2), listOfGroups[0].toList())
        Assert.assertEquals(listOf(3,4), listOfGroups[1].toList())
        Assert.assertEquals(listOf(5,6), listOfGroups[2].toList())
        Assert.assertEquals(listOf(7,8), listOfGroups[3].toList())
        Assert.assertEquals(listOf(9,10), listOfGroups[4].toList())
    }

    @Test fun testSpecificCase() {
        val originalStream = listOf(1,2,3,4,5,6,7,8,9,10)

        val results = originalStream.asSequence().batch(3).map { group ->
            group.toList()
        }.toList()

        Assert.assertEquals(listOf(1,2,3), results[0])
        Assert.assertEquals(listOf(4,5,6), results[1])
        Assert.assertEquals(listOf(7,8,9), results[2])
        Assert.assertEquals(listOf(10), results[3])
    }


    fun testStream(testList: List<Int>, batchSize: Int, expectedGroups: Int) {
        var groupSeenCount = 0
        val itemsSeen = ArrayList<Int>()

        testList.asSequence().batch(batchSize).forEach { groupStream ->
            groupSeenCount++
            groupStream.forEach { item ->
                itemsSeen.add(item)
            }
        }

        Assert.assertEquals(testList, itemsSeen)
        Assert.assertEquals(groupSeenCount, expectedGroups)
    }

    @Test fun groupsOfExactSize() {
        testStream(listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15), 5, 3)
    }

    @Test fun groupsOfOddSize() {
        testStream(listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18), 5, 4)
        testStream(listOf(1,2,3,4), 3, 2)
    }

    @Test fun groupsOfLessThanBatchSize() {
        testStream(listOf(1,2,3), 5, 1)
        testStream(listOf(1), 5, 1)
    }

    @Test fun groupsOfSize1() {
        testStream(listOf(1,2,3), 1, 3)
    }

    @Test fun groupsOfSize0() {
        val testList = listOf(1,2,3)

        val groupCountZero =   testList.asSequence().batch(0).toList().size
        Assert.assertEquals(0, groupCountZero)

        val groupCountNeg =  testList.asSequence().batch(-1).toList().size
        Assert.assertEquals(0, groupCountNeg)

    }

    @Test fun emptySource() {
        listOf<Int>().asSequence().batch(1).forEach { fail() }
    }
}