#!/usr/bin/python

topics = [
	{
		'title': "What is SMS Time Fix?",
		'body': '''
			SMS Time Fix was built to solve one problem: receiving text messages with incorrect times. For me, this was caused by using an unlocked Telus phone on Rogers. For others, this is about roaming on Sprint. Whatever the reason may be, if you receive messages with the incorrect time, SMS Time Fix should solve your problem.
		''',
	},
	{
		'title': "How does it work?",
		'body': '''
			SMS Time Fix listens to the SMS database for any changes. Once a new message comes in, SMS Time Fix wakes up and alters the time of the message. There are many different settings for how and when SMS Time Fix adjusts your messages. Continue reading to find out more.
		''',
	},
	{
		'title': "Limitations",
		'body': '''
			SMS Time Fix uses the time already assigned to the message to calculate the correct time. This means that the error must be consistent. e.g. If your message time is wrong, it is always off by 5 hours. The only exception to this is if you want to ignore the provided time and always use your phone time. See <i>Adjustment Methods</i> for more information.<br /><br />

			If messages sent while your phone is turned off (or disconnected) show the time that your phone was turned on (connected), SMS Time Fix is unable to accurately determine when the message was sent. Please contact your network provider and your device manufacturer and let them know that you are not satisfied with the way your phone and the network have been configured.
		''',
	},
	{
		'title': "Permissions",
		'body': '''
			<dl>
				<dt>WRITE_SMS</dt>
				<dd>Required to adjust messages.</dd>
			</dl>
			<dl>
				<dt>READ_SMS</dt>
				<dd>Required to read messages.</dd>
			</dl>
			<dl>
				<dt>WRITE_EXTERNAL_STORAGE</dt>
				<dd>Legacy permission from when I was writing logs to the SD card. Required for the current release to delete the leftover log files. This will be removed in the next update.</dd>
			</dl>
			<dl>
				<dt>RECEIVE_BOOT_COMPLETED</dt>
				<dd>Required to start the service after the phone is turned on or restarted, and also after the app has been updated.</dd>
			</dl>
		''',
	},
	{
		'title': "Adjustment Methods",
		'body': '''
			There are currently four different methods for adjusting the times of your new methods.

			<dl>
				<dt>Add Time Zone</dt>
				<dd>
					This will add your GMT offset to the message time.<br /><br />

					e.g. If you are GMT-5 or GMT+5, 5 hours will be ADDED to all incoming messages. 
				</dd>
				
				<dt>Subtract Time Zone</dt>
				<dd>
					This will subtract your GMT offset from the message time.<br /><br />

					e.g. If you are GMT-3 or GMT+3, 3 hours will be SUBTRACTED from all incoming messages.
				</dd>
				
				<dt>Add Manual Offset</dt>
				<dd>
					This allows you to specify how many hours (and minutes) you'd like to adjust your messages by. When this method is selected, choose your offset by selecting <i>Offset Hours</i> and <i>Offset Minutes</i> from the main window.<br /> <br />

					<b>NOTE</b> that the offset can be negative!
				</dd>
				
				<dt>Use Phone's Time</dt>
				<dd>
					This method uses the phone's current time for all incoming messages. Keep in mind, however, that this method completely ignores when the network says the message was sent when determining what to set the time to. 
					Consider this a last resort.
				</dd>
			</dl>
		''',
	},
	{
		'title': "Future Only",
		'body': '''
			Enabling this option will only adjust a message if the provided time is in the future.
		''',
	},
	{
		'title': "Roaming Only",
		'body': '''
			Enabling this option will only adjust a message if you are currently roaming. <br /><br />
			
			<b>NOTE:</b> I'm unable to do any real testing of this setting since I do not experience this issue. 
			I cannot guarantee that this method works at all. If you decide to try it out, please let me know the results!
		''',
	},
	{
		'title': "Running Notification",
		'body': '''
			This adds an ongoing notification to your notification bar. This allows you to see whether or not SMS Time Fix is currently running. Having an ongoing notification also marks the app as a high priority and will prevent the native Android task manager from shutting it down. If you're having any issues where SMS Time Fix stops working, please enable this notification!
		''',
	},
	{
		'title': "Fixing Old Messages",
		'body': '''
			This tool allows you to adjust any messages recieved prior to using SMS Time Fix, or any messages the SMS Time Fix missed. See the <i>Fix Old Messages</i> window in the app for more information.<br /><br />

			PLEASE PLEASE PLEASE back up your messages before you run this tool. Many things can go wrong while the tool is running and there's no undo function in place. There are numerous SMS backup apps in the Play Store that make it really easy to do this. Pick one and make a backup. After that, make another.
		''',
	},
	{
		'title': "My conversations are all out of order after fixing old messages",
		'body': '''
			This is a known issue and there currently isn't anything I can do to fix it. The data for conversations is stored separately from the individual text messages and developers do not have write-access to this data. Once you receive a new message inside of a conversation, the time will be properly updated. So, give it some time and most (if not all) of your conversations will be corrected to the right order.
		''',
	},
	{
		'title': "SMS Time Fix keeps turning itself off",
		'body': '''
			Enable the <i>Running Notification</i> option.
		''',
	},
	{
		'title': "SMS Time Fix doesn't start after I reboot my phone",
		'body': '''
			Make sure that you haven't forced the app to the SD card. Apps that have been moved to SD are unable to be started automatically when the phone is turned on. If it hasn't been moved to SD, try reinstalling the app.
		''',
	},
	{
		'title': "Can I contribute?",
		'body': '''
			You most certainly can! The source can be found on <a href="http://github.com/mattprecious/smstimefix">GitHub</a>. Feel free to fork the repo and submit any changes back to me!
		''',
	},
	{
		'title': "Can you translate SMS Time Fix to my language?",
		'body': '''
			I can't, but you can! Contribute translations through <a href="http://crowdin.net/project/sms-time-fix" />Crowdin</a>.
		''',
	},
	{
		'title': "Can I donate?",
		'body': '''
			Yes, yes you can! You can find a donate version of the app on the market <a href="https://market.android.com/details?id=com.mattprecious.smsfixdonate">right here</a>.If you feel that the app deserves more (or less) than $1, you can make a donation through PayPal:<br /><br />

			${PAYPAL}
		''',
	},
	{
		'title': "I have a feature request",
		'body': '''
			<a href="/contact">Contact me</a>
		''',
	},
	{
		'title': "These don't solve my problem",
		'body': '''
			Please contact me. There are two ways to contact me for help. For both methods, describe your problem in as much detail as possible. The more I know about your issue right off the start, the less questions I have to ask you. Also, please provide information about your phone (model, Android version, etc.).

			<ol>
				<li>
					Open the app and select <i>Email Developer</i>. This will automatically add information about your device and your settings to the email.
				</li>
				<li>
					<a href="/contact">Fill out this form</a>.
				</li>
			</ol>
		''',
	},
]

from wheezy.template.engine import Engine
from wheezy.template.ext.core import CoreExtension
from wheezy.template.loader import FileLoader

searchpath = ['.']
engine = Engine(
    loader=FileLoader(searchpath),
    extensions=[CoreExtension()]
)
template = engine.get_template('template.html')

f = open('smsfix.html', 'w')
f.write(template.render({'pagetitle': 'SMS Time Fix Help', 'topics': topics}))
f.close()