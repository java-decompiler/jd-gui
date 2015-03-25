/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.bean

import jd.gui.api.model.Container

import javax.swing.*

class OpenTypeListCellBean {
    String label
    String packag
    Icon icon
    Collection<Container.Entry> entries
    String typeName
}
