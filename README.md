This repo demonstrates how to run functional tests using [Geb](http://www.gebish.org) with multiple browsers and windows. [ChatPageSpec.groovy](https://github.com/kensiprell/geb-multibrowser/blob/master/ChatPageSpec.groovy) opens two browsers that also open an additional window each for a total of four open windows. It shows how to move around in the different windows.


It extends [Alex Anderson's classes](https://github.com/alxndrsn/geb-multibrowser) although I had to change [line 41](https://github.com/kensiprell/geb-multibrowser/blob/master/MultiBrowserGebSpec.groovy#L41) or the additional windows are left open when the tests end. 

I also modified [withBrowserSession()](https://github.com/kensiprell/geb-multibrowser/blob/master/MultiBrowserGebSpec.groovy#L29) so that it returns an object. Although not demonstrated in the Spec, you could use something like the feature method below.

```
def "test using only an expect"() {
    expect:
        withBrowserSession b, {
            withWindow(windowHandleOrName, page: OnePage) {
                linkTwo.text()
            }
        } == "Two"
}
```


