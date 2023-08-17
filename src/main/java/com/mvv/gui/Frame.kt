package com.mvv.gui

import javafx.application.Application
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage


class Frame() : Application() {

    override fun start(stage: Stage) {
        //m2(stage)
        mWords(stage)
    }

    private fun mWords(primaryStage: Stage) {
        val scene = Scene(MainWordsPane())
        primaryStage.setScene(scene)
        primaryStage.title = appTitle

        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun m2(primaryStage: Stage) {

        val gridPane = GridPane()
        gridPane.alignment = Pos.CENTER
        gridPane.hgap = 10.0
        gridPane.vgap = 10.0
        gridPane.padding = Insets(25.0, 25.0, 25.0, 25.0)

        gridPane.border = Border.stroke(Color.AQUA)
        //gridPane.border = Border(arrayOf(BorderStroke.MEDIUM))
        //gridPane.border = Border(strokes = arrayOf(BorderStroke.MEDIUM))
        //val aaa: Array<BorderStroke> = arrayOf(BorderStroke.MEDIUM)
        //val aaa: Array<BorderStroke> = arrayOf(BorderStroke(null, BorderStrokeStyle.SOLID, null, BorderWidths(25.0)))
        //gridPane.border = Border(*aaa)
        gridPane.border = Border(BorderStroke(Color.AQUA, BorderStrokeStyle.SOLID, null, BorderWidths(10.0)))
        //gridPane.border = Border(*BorderStroke.MEDIUM)
        //gridPane.border = Border(*BorderStroke.MEDIUM)

        val scene = Scene(gridPane) // , 300.0, 275.0)
        primaryStage.setScene(scene)

        //scene.stylesheets.add("fuck")
        //scene.stylesheets.add("-fx-padding: 10;" +
        //        "-fx-border-style: solid inside;" +
        //        "-fx-border-width: 2;" +
        //        "-fx-border-insets: 5;" +
        //        "-fx-border-radius: 5;" +
        //        "-fx-border-color: cyan;")

        //val root = VBox(10.0)
        //root.style

        val sceneTitle = Text("Welcome")
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20.0))
        gridPane.add(sceneTitle, 0, 0, 2, 1)

        val userName = Label("User Name:")
        gridPane.add(userName, 0, 1)

        val userTextField = TextField()
        gridPane.add(userTextField, 1, 1)

        val pw = Label("Password:")
        gridPane.add(pw, 0, 2)

        val pwBox = PasswordField()
        gridPane.add(pwBox, 1, 2)

        //gridPane.columnConstraints[1].hgrow = Priority.ALWAYS
        gridPane.columnConstraints.add(0, ColumnConstraints(-1.0, -1.0, -1.0, Priority.NEVER, HPos.RIGHT, false))
        gridPane.columnConstraints.add(1, ColumnConstraints(-1.0, -1.0, 1000.0, Priority.ALWAYS, HPos.LEFT, true))

        primaryStage.scene = scene
        //primaryStage.
        primaryStage.show()
    }

    private fun m4(primaryStage: Stage) {

        val gridPane = GridPane()
        gridPane.alignment = Pos.CENTER
        gridPane.hgap = 10.0
        gridPane.vgap = 10.0
        gridPane.padding = Insets(25.0, 25.0, 25.0, 25.0)

        gridPane.border = Border.stroke(Color.AQUA)
        //gridPane.border = Border(arrayOf(BorderStroke.MEDIUM))
        //gridPane.border = Border(strokes = arrayOf(BorderStroke.MEDIUM))
        //val aaa: Array<BorderStroke> = arrayOf(BorderStroke.MEDIUM)
        //val aaa: Array<BorderStroke> = arrayOf(BorderStroke(null, BorderStrokeStyle.SOLID, null, BorderWidths(25.0)))
        //gridPane.border = Border(*aaa)
        gridPane.border = Border(BorderStroke(Color.AQUA, BorderStrokeStyle.SOLID, null, BorderWidths(10.0)))
        //gridPane.border = Border(*BorderStroke.MEDIUM)
        //gridPane.border = Border(*BorderStroke.MEDIUM)

        val scene = Scene(gridPane) // , 300.0, 275.0)
        primaryStage.setScene(scene)

        //scene.stylesheets.add("fuck")
        //scene.stylesheets.add("-fx-padding: 10;" +
        //        "-fx-border-style: solid inside;" +
        //        "-fx-border-width: 2;" +
        //        "-fx-border-insets: 5;" +
        //        "-fx-border-radius: 5;" +
        //        "-fx-border-color: cyan;")

        //val root = VBox(10.0)
        //root.style

        val sceneTitle = Text("Welcome")
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20.0))
        gridPane.add(sceneTitle, 0, 0, 2, 1)

        val userName = Label("User Name:")
        gridPane.add(userName, 0, 1)

        val userTextField = TextField()
        gridPane.add(userTextField, 1, 1)

        val pw = Label("Password:")
        gridPane.add(pw, 0, 2)

        val pwBox = PasswordField()
        gridPane.add(pwBox, 1, 2)

        //gridPane.columnConstraints[1].hgrow = Priority.ALWAYS
        gridPane.columnConstraints.add(0, ColumnConstraints(-1.0, -1.0, -1.0, Priority.NEVER, HPos.RIGHT, false))
        gridPane.columnConstraints.add(1, ColumnConstraints(-1.0, -1.0, 1000.0, Priority.ALWAYS, HPos.LEFT, true))

        primaryStage.scene = scene
        //primaryStage.
        primaryStage.show()
    }

    override fun init() {
        super.init()
    }

    private fun m1(stage: Stage) {
        val javaVersion = System.getProperty("java.version")
        val javafxVersion = System.getProperty("javafx.version")
        val l = Label("Hello, JavaFX $javafxVersion, running on Java $javaVersion.")
        val scene = Scene(StackPane(l), 640.0, 480.0)
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    //SvgImageLoaderFactory.install()
    //SvgImageLoaderFactory.install(PrimitiveDimensionProvider())

    Application.launch(Frame::class.java)
}
