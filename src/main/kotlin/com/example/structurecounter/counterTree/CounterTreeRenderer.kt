package com.example.structurecounter.counterTree

import com.example.structurecounter.model.CounterEntryType
import com.intellij.icons.AllIcons
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.TreeCellRenderer

/**
 * Renders CounterTreeNode with all properties and corresponding icon.
 */
class CounterTreeRenderer : JLabel(), TreeCellRenderer {
    private val iconLabel = JLabel()
    private val nameLabel = JLabel()
    private val classCounterLabel = JLabel()
    private val functionCounterLabel = JLabel()

    // I've encountered a very wierd issue when nothing was rendered if nothing is altered explicitly in `this`
    // to make the interface a bit nicer and apply layout I've decided to place the rendered info into extra panel
    private val outerPanel = JPanel()

    init {
        outerPanel.layout = FlowLayout(FlowLayout.LEFT)

        val c = GridBagConstraints()
        c.anchor = GridBagConstraints.BASELINE
        c.gridx = 0
        c.gridy = 0
        c.insets = JBUI.insetsRight(5)
        outerPanel.add(iconLabel, c)

        c.gridx = 1
        c.insets = JBUI.insetsRight(10)
        outerPanel.add(nameLabel, c)

        c.gridx = 2
        c.insets = JBUI.insetsRight(5)
        outerPanel.add(classCounterLabel, c)

        c.gridx = 3
        c.insets = JBUI.emptyInsets()
        outerPanel.add(functionCounterLabel, c)
    }

    override fun getTreeCellRendererComponent(
        tree: JTree, value: Any,
        selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int,
        hasFocus: Boolean
    ): Component {
        var icon: Icon? = null
        var classLabelText = ""
        var functionLabelText = ""
        val nameLabelText = value.toString()
        if (value is CounterTreeNode) {
            icon = when (value.type) {
                CounterEntryType.Module -> AllIcons.Nodes.Module
                CounterEntryType.SourceFile -> AllIcons.FileTypes.Java
                CounterEntryType.Class -> AllIcons.FileTypes.JavaClass
                CounterEntryType.Package -> AllIcons.Nodes.Package
                CounterEntryType.Function -> AllIcons.Nodes.Method
            }

            if (value.type != CounterEntryType.Function) {
                classLabelText = "| Classes: " + value.classCount
                functionLabelText = "| Methods: " + value.functionCount
            }
        }
        iconLabel.icon = icon
        nameLabel.text = nameLabelText
        classCounterLabel.text = classLabelText
        functionCounterLabel.text = functionLabelText
        isEnabled = tree.isEnabled
        outerPanel.font = tree.font
        return outerPanel
    }
}
