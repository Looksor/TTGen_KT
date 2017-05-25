package ru.inforion.ttgen.general

import org.apache.log4j.LogManager
import org.hibernate.cfg.Environment
import ru.inforion.ttgen.impl.AbstractTTGen
import ru.inforion.ttgen.impl.AutoTTGen
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val logger = LogManager.getLogger(AbstractTTGen::class.java)
    logger.info("Логгер TTGen активирован")
    if (args.isEmpty()) {
        logger.error("Не указан параметр jdbcUrl.")
        return
    }

    val jdbcUrl = args[0]

    val props = mapOf(
        Environment.JPA_TRANSACTION_TYPE to "RESOURCE_LOCAL",
        Environment.JPA_JDBC_URL to jdbcUrl,
        Environment.JPA_JTA_DATASOURCE to null,
        Environment.JPA_JDBC_USER to "AUTO_TIMETABLE_NEW",
        Environment.JPA_JDBC_PASSWORD to "AUTO_TIMETABLE_NEW"
    )

    try {
        val autoTTGen = AutoTTGen(logger, props)
        autoTTGen.run()
    } catch (e : Exception) {
        logger.error("Ошибка работы генератора АВТО расписаний. ", e)
    }

    // TODO Ship

    // TODO Rail

    exitProcess(0)
}



