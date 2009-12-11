Selenium.prototype.ensureNoUnhandledPopups = function() {
    if (this.browserbot.hasAlerts()) {
        throw new SeleniumError("There was an unexpected Alert! [" + this.browserbot.getNextAlert() + "]");
    }
    if ( this.browserbot.hasConfirmations() ) {

    }
};

