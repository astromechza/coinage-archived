package org.coinage.gui.components;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Created At: 2016-11-15
 */
public class HExpander extends Region
{
    public HExpander()
    {
        HBox.setHgrow(this, Priority.ALWAYS);
    }
}
