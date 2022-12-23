package com.example

import org.h2.api.Trigger
import java.sql.Connection

class DeleteTrigger : Trigger {
    override fun fire(conn: Connection?, oldRow: Array<out Any>?, newRow: Array<out Any>?) {
        val sql = """
            DELETE FROM aidedatas
            WHERE TIMESTAMPDIFF(HOUR, date_created, CURRENT_TIMESTAMP()) >= 24 
        """
        conn?.prepareStatement(sql)
    }

}