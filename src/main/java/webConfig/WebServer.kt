package main.java.webConfig

import io.javalin.Context
import io.javalin.Javalin
import main.java.extension.LOG
import main.java.manager.DataHolder
import main.java.settings.Settings
import settings.ManagementMode


class WebServer {
    lateinit var app: Javalin

    init {
        //to disable jetty logging
        org.eclipse.jetty.util.log.Log.setLog(NoLogging())

        if (Settings.shared.port > -1) {
            app = Javalin.create().disableStartupBanner()
            app.get("/") { ctx -> ctx.html(html) }
            app.get("style.css") { ctx ->
                ctx.result(css)
                ctx.contentType("text/css")
            }
            app.get("/config") { ctx -> getConfig(ctx) }
            app.get("/setconfig") { ctx -> setConfig(ctx) }
            app.get("/boxstate") { ctx -> getCurrentPower(ctx) }
            app.start(Settings.shared.port)
            LOG().log("REST API active. The following endpoints are available:\n" +
                    "/config responds with the current configuration\n" +
                    "/setconfig sets a new config for a given field, responds with the current config\n" +
                    "Currently supported: mode.\n" +
                    "Example: /setconfig?mode=OFF")
        }
    }

    fun getCurrentPower(ctx: Context) {
        ctx.result(DataHolder.shared.toString())
    }

    fun getConfig(ctx: Context) {
        ctx.result(Settings.shared.toString())
    }

    fun setConfig(ctx: Context) {
        val queryParamMap = ctx.queryParamMap()
        if (queryParamMap.count() == 0) {
            ctx.status(404)
            ctx.result("Params missing")
            return
        }

        for (key in queryParamMap.keys) {

            //Mode handling ===============
            if (key == "mode") {
                val mode = queryParamMap.get(key)?.get(0)
                when (mode) {
                    "OFF" -> Settings.shared.mode = ManagementMode.OFF
                    "PV" -> Settings.shared.mode = ManagementMode.PV
                    "PV_WITH_MIN" -> Settings.shared.mode = ManagementMode.PV_WITH_MIN
                    "MAX" -> Settings.shared.mode = ManagementMode.MAX
                    else -> {
                        ctx.result("Unknown Mode: $mode")
                        ctx.status(400)
                        return
                    }
                }
                LOG().log("API: Mode set: $mode")
                ctx.status(200)
                ctx.result(Settings.shared.toString())
                return
            }
            // ==============================
            //minValueHandling
            if (key == "minPowerKiloWatt") {
                val minPower = queryParamMap.get(key)?.get(0)
                if (minPower != null) {
                    try {
                        val minPowerFloat = minPower.toFloat()
                        Settings.shared.minPowerKiloWatt = minPowerFloat
                        LOG().log("Minimum Power set: $minPowerFloat kW")
                        ctx.status(200)
                        ctx.result(Settings.shared.toString())
                    } catch (e: NumberFormatException) {
                        ctx.result("Not a numerical value: $minPower")
                        ctx.status(400)
                        return
                    }
                } else {
                    ctx.result("please provide a value")
                    ctx.status(400)
                    return
                }
                return
            }
            // ==============================
            //maxPowerHandlong
            if (key == "maxPowerKiloWatt") {
                val minPower = queryParamMap.get(key)?.get(0)
                if (minPower != null) {
                    try {
                        val minPowerFloat = minPower.toFloat()
                        Settings.shared.maxPowerKiloWatt = minPowerFloat
                        LOG().log("Minimum Power set: $minPowerFloat kW")
                        ctx.status(200)
                        ctx.result(Settings.shared.toString())
                    } catch (e: NumberFormatException) {
                        ctx.result("Not a numerical value: $minPower")
                        ctx.status(400)
                        return
                    }
                } else {
                    ctx.result("please provide a value")
                    ctx.status(400)
                    return
                }
                return
            }


        }

        ctx.status(400)
        ctx.result("Unknown parameter")

    }
}