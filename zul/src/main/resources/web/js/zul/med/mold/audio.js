/* audio.js

	Purpose:
		
	Description:
		
	History:
		Thu Mar 26 11:59:58     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under LGPL Version 2.1 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
function audio$mold$(out) {
	out.push('<audio', this.domAttrs_(), '>', this.domContent_());
	for (var w = this.firstChild; w; w = w.nextSibling)
		w.redraw(out);
	out.push('</audio>')
}