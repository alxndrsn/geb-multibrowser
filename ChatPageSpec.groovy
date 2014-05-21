package tests

import geb.*
import geb.spock.MultiBrowserReportingGebSpec
import pages.ChatPage
import pages.ChatStartPage
import pages.NavigationPage
import org.openqa.selenium.Keys
import org.openqa.selenium.Point
import spock.lang.*

@Stepwise
class ChatPageSpec extends MultiBrowserReportingGebSpec {
    
    def "two users login"() {
        given: "userOne and userTwo are logged in"
            withBrowserSession a, {
                driver.manage().window().setPosition(new Point(0, 0))
                login("userOne", "password1!", false)
            }
            withBrowserSession b, {
                driver.manage().window().setPosition(new Point(850, 0))
                login("userTwo", "password1!", false)
            }
    }

    def "user sends chat request"() {
        when: "userTwo sends chat request"
            b.to ChatStartPage
            b.page.sendChatRequest.click()
            b.waitFor { b.page.notifications.notificationsBadge.text() == "1" }
            b.page.notifications.collapse.click()

        then: "'Open Chat' notification is displayed in userTwo's browser"
            b.page.openChat.displayed
    }
    
    def "user sends message"() {
        def chatMessageText
        
        when: "userTwo sends 'hello'"
            withBrowserSession b, {
                withWindow({ title == "Chat" }, page: ChatPage) {
                    chatInput << "hello" + Keys.RETURN
                    waitFor { chatWindowLive.children().size() > 0 }
                    chatMessageText = chatMessage.text()
                }
            }
            
        then: "'hello' is returned to userTwo's chat session"
            chatMessageText == ": hello"
    }
    
    def "user receives chat request"() {        
        when: "userOne receives chat request"
            a.to ChatStartPage
            a.waitFor { a.page.notifications.notificationsBadge.text() == "1" }
            a.page.notifications.collapse.click()

            
        then: "'Start Chat' notification is displayed in userOne's browser"
            a.page.startChat.displayed    
    }
    
    def "user accepts chat request"() {
        def chatMessageText
        def iqMessageText
        
        when: "userOne clicks on 'Start Chat' link"
            a.page.startChat.click()
            withBrowserSession a, {
                withWindow({ title == "Chat" }, page: ChatPage) {
                    waitFor { chatWindowLive.children().size() > 0 }
                    chatMessageText = chatMessage.text()
                }
            }
            withBrowserSession b, {
                withWindow({ title == "Chat" }, page: ChatPage) {
                    waitFor { chatWindowLive.children().size() > 1 }
                    iqMessageText = iqMessage.text()
                }
            }

        then: "userOne's chat window opens and receives chat history; userTwo receives 'userOne joined' message"
            chatMessageText == ": hello"
            iqMessageText == "userOne joined"
    }
    
    def "user returns chat message"() {
        def chatMessageTextSent
        def chatMessageTextReceived
        
        when: "userOne sends 'hi'"
            withBrowserSession a, {
                withWindow({ title == "Chat" }, page: ChatPage) {
                    chatInput << "hi" + Keys.RETURN
                    waitFor { chatWindowLive.children().size() > 1 }
                    chatMessageTextSent = chatMessage[1].text()
                }
            }
            withBrowserSession b, {
                withWindow({ title == "Chat" }, page: ChatPage) {
                    waitFor { chatWindowLive.children().size() > 2 }
                    chatMessageTextReceived = chatMessage[1].text()
                }
            }

        then: "'hi' is returned to userOne's chat session; userTwo receives message"
            chatMessageTextSent == ": hi"
            chatMessageTextReceived == ": hi"
    }
    
    def "user closes browser"() {
        def iqMessageText
        
        when: "userOne closes browser"
            withBrowserSession a, {
                withWindow({title == "Chat"}) {
                    browser.close()
                }
            }
            withBrowserSession b, {
                withWindow({ title == "Chat" }, page: ChatPage) {
                    waitFor { chatWindowLive.children().size() > 3 }
                    iqMessageText = iqMessage[1].text()
                }
            }

        then: "userTwo receives 'userOne left' message"
            iqMessageText == "userOne left"
    }
    
    void login(String user, String password, boolean rememberMe) {
        NavigationPage navigationPage = to NavigationPage
        navigationPage.navigation.showLoginModal.click()
        waitFor { navigationPage.loginModal.loginModal.displayed }
        navigationPage.loginModal.inputUsername.value(user)
        navigationPage.loginModal.inputPassword.value(password)
        navigationPage.loginModal.inputRememberMe.value(rememberMe)
        navigationPage.loginModal.buttonLogin.click()
        waitFor { navigationPage.navigation.greetingText.text().endsWith(user) }
    }
}
