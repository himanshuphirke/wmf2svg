This project has moved to wmf2svg.sourceforge.jp ( http://wmf2svg.sourceforge.jp/ ).

---

This project's goal is to make tool & library for converting wmf to svg.

Example: `java -jar wmf2svg-0.9.5.jar [options...] [wmf filename] [svg filename]`

Options:
> `-compatible`: output IE9 compatible style. but it's dirty and approximative.

If you need to compress by gzip, you should use .svgz suffix as svg filename.

It's necessary to Java 1.4 or later.

  * 2013-09-29: Fixed a invalid css property.
  * 2012-08-28: Improved arc radious and Fixed negative size image bug.
  * 2012-07-05: Improved arc problem, and fixed TextOut background position bug.
  * 2012-02-11: Improved arc problem, and fixed font-size bug.
  * 2011-09-28: Fixed restoreDC and cyrillic bug. And add wmf writer function.
  * 2011-08-20: Some bugs fixed.
  * 2011-03-10: Arc bug fixed and extTextOut is enhanced.
  * 2011-01-08: Arc bug fixed and add fillRgn/paintRgn support.
  * 2010-12-01: Some bugs fixed.
  * 2010-11-20: Text position bug fixed.
  * 2010-09-11: Some bugs fixed.
  * 2009-05-24: wmf2svg version 0.8.3 is supported on Google App Engine/Java.

Copyright (c) 2007-2011 Hidekatsu Izuno, Shunsuke Mori All right reserved.