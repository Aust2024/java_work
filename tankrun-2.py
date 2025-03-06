import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
import json

_g_cst_ToMQTTTopicServerIP = 'localhost' #ServerIP
_g_cst_ToMQTTTopicServerPort = 1883 #Port
_g_cst_ToMQTTTopicName = 'Tank' #Sensor Article主題
GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)

motor1a = 7
motor1b = 11
motor1e = 22

motor2a = 13
motor2b = 16
motor2e = 15

GPIO.setup(motor1a,GPIO.OUT)
GPIO.setup(motor1b,GPIO.OUT)
GPIO.setup(motor1e,GPIO.OUT)
GPIO.setup(motor2a,GPIO.OUT)
GPIO.setup(motor2b,GPIO.OUT)
GPIO.setup(motor2e,GPIO.OUT)

def on_connect(client, userdata, rc):
    print ("Connected with rc: " + str(rc))
    

def on_subscribe(client, userdata, mid, granted_qos):
    print('{} '.format(userdata))

def on_publish(client, userdata, mid):
    print('published ')

def on_message(client, userdata, msg):
    print ("Topic: "+ msg.topic+"\nMessage: "+str(msg.payload))
    
    if 'w' in str(msg.payload):
        GPIO.output(motor1a,GPIO.HIGH)
        GPIO.output(motor1b,GPIO.LOW)
        GPIO.output(motor1e,GPIO.HIGH)
        GPIO.output(motor2a,GPIO.HIGH)
        GPIO.output(motor2b,GPIO.LOW)
        GPIO.output(motor2e,GPIO.HIGH)
    elif 's' in str(msg.payload):
        GPIO.output(motor1a,GPIO.LOW)
        GPIO.output(motor1b,GPIO.HIGH)
        GPIO.output(motor1e,GPIO.HIGH)
        GPIO.output(motor2a,GPIO.LOW)
        GPIO.output(motor2b,GPIO.HIGH)
        GPIO.output(motor2e,GPIO.HIGH)
    elif 'a' in str(msg.payload):   
        GPIO.output(motor1a,GPIO.HIGH)
        GPIO.output(motor1b,GPIO.LOW)
        GPIO.output(motor1e,GPIO.HIGH)
        GPIO.output(motor2a,GPIO.LOW)
        GPIO.output(motor2b,GPIO.HIGH)
        GPIO.output(motor2e,GPIO.HIGH)
    elif 'd' in str(msg.payload):
        GPIO.output(motor1a,GPIO.LOW)
        GPIO.output(motor1b,GPIO.HIGH)
        GPIO.output(motor1e,GPIO.HIGH)
        GPIO.output(motor2a,GPIO.HIGH)
        GPIO.output(motor2b,GPIO.LOW)
        GPIO.output(motor2e,GPIO.HIGH)
    elif 'm' in str(msg.payload):
        GPIO.output(motor1a,GPIO.LOW)
        GPIO.output(motor1b,GPIO.LOW)
        GPIO.output(motor1e,GPIO.LOW)
        GPIO.output(motor2a,GPIO.LOW)
        GPIO.output(motor2b,GPIO.LOW)
        GPIO.output(motor2e,GPIO.LOW)

if __name__ == '__main__':    
    try:
        client = mqtt.Client(_g_cst_ToMQTTTopicName)

        client.on_connect = on_connect
        client.on_message = on_message
        client.on_subscribe = on_subscribe
        client.on_publish = on_publish
        client.connect(_g_cst_ToMQTTTopicServerIP, _g_cst_ToMQTTTopicServerPort,60)
        #client.subscribe(_g_cst_ToMQTTTopicName)
        client.subscribe([(_g_cst_ToMQTTTopicName,0),("Infrared",0)])
        client.loop_forever()

    except KeyboardInterrupt:
        GPIO.cleanup()
        print("clean GPIO")
