Automatically help you complete the path of assets when you type a Dart string
------------------------------------------------------------------------------

![demo](https://i.imgur.com/Ssc3eMJ.gif)

## Register assets to pubspec with one click!

![demo](https://i.imgur.com/tM6yqKb.gif)

### Support preview of multiple image formats such as png, svg, jpg, gif and more

### Support for assets declared in pubspec.yaml

*   normal assets like:

```
        assets:
          - path/to/assets_directory
          - path/to/image.png
```    

*   assets in "lib" directory like:

```
        assets:
          - image.png
```
 
*   assets declared in pubspec.yaml of other packages.

*   assets in lib/ of other packages and declared in pubspec.yaml of current package like:

```    
        assets:
          - packages/fancy_images_lib/image.png
```    

### Support for fonts

*   fonts declared in pubspec.yaml of other packages.

*   normal fonts declared in pubspec.yaml like:

```    
        fonts:
          - family: Andale Mono
            fonts:
              - asset: fonts/Andale Mono.ttf
```    

*   iOS pre-installed fonts:
```
    IOS_9_FONT_LIST:
              "PingFang HK",
              "PingFang SC",
              "PingFang TC",
              "Kohinoor Bangla",
              "Hiragino Sans",
    
    IOS_8_FONT_LIST:
              "Academy Engraved LET",
              "Al Nile",
              "American Typewriter",
              "Apple Color Emoji",
              "Apple SD Gothic Neo",
              "Arial",
              "Arial Hebrew",
              "Arial Rounded MT Bold",
              "Avenir",
              "Avenir Next",
              "Avenir Next Condensed",
              "Bangla Sangam MN",
              "Baskerville",
              "Bodoni Ornaments",
              "Bradley Hand",
              "Chalkboard SE",
              "Chalkduster",
              "Cochin",
              "Copperplate",
              "Courier",
              "Courier New",
              "DB LCD Temp",
              "DIN Alternate",
              "DIN Condensed",
              "Damascus",
              "Devanagari Sangam MN",
              "Didot",
              "Diwan Mishafi",
              "Euphemia UCAS",
              "Farah",
              "Futura",
              "Geeza Pro",
              "Georgia",
              "Gill Sans",
              "Gujarati Sangam MN",
              "Gurmukhi MN",
              "Heiti SC",
              "Heiti TC",
              "Helvetica",
              "Helvetica Neue",
              "Hiragino Kaku Gothic ProN",
              "Hiragino Mincho ProN",
              "Hoefler Text",
              "Iowan Old Style",
              "Kailasa",
              "Kannada Sangam MN",
              "KhmerSangamMN",
              "KohinoorDevanagari",
              "Kohinor Telugu",
              "LaoSangamMN",
              "Malayalam Sangam MN",
              "Marion",
              "Marker Felt",
              "Menlo",
              "Noteworthy",
              "Optima",
              "Oriya Sangam MN",
              "Palatino",
              "Papyrus",
              "Party LET",
              "San Francisco",
              "Savoye Let",
              "Sinhala Sangam MN",
              "Snell Roundhand",
              "Superclarendon",
              "Symbol",
              "Tamil Sangam MN",
              "Telugu Sangam MN",
              "Thonburi",
              "Times New Roman",
              "Trebuchet MS",
              "Verdana",
              "Zapf Dingbats",
              "Zapfino",
```    

*   Android pre-installed fonts:

```
              "sans-serif",
              "sans-serif-light",
              "sans-serif-thin",
              "sans-serif-condensed",
              "serif",
              "Droid Sans",
```              


## Thanks
The svg decoder code is from the open source project 'Volantis Mobility Server CE',
its source code can be found here: https://sourceforge.net/projects/volmobserverce/