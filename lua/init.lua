wifi.setmode(wifi.STATION)
-- wifi.sta.config("esp2323", "asd12345")
wifi.sta.config("ISS-4", "Nyzarak3132")
-- wifi.sta.config("Xperia Z3 Compact_1e7c", "40055dfe33b5")

proximity_detector_timer_id = 3
proximity_detector_status = false
proximity_detector_color_active = "0"
deviceName = "Device no.1"
ver = "ver. 1.0"
sensitivity_value = 0

-- functions

id=0
sda=6
scl=7
i2c.setup(id,sda,scl,i2c.SLOW)
function read_reg(dev_addr, reg_addr)
	i2c.start(id)
	i2c.write(id,dev_addr)
	i2c.write(id,reg_addr)
	i2c.start(id)
	i2c.write(id,0x51)
	c=i2c.read(id,1)
	i2c.stop(id)
	return c
end
function write_reg(dev_addr, reg_addr, reg_data)
	i2c.start(id)
	i2c.write(id,dev_addr)
	i2c.write(id,reg_addr)
	i2c.write(id,reg_data)
	i2c.stop(id)
end

-- LED
R=2
G=8
B=1
pwm.setup(R, 100, 1)
pwm.setup(G, 100, 1)
pwm.setup(B, 100, 1)
global_r='0'
global_g='0'
global_b='0'


cc = coap.Client()		
tmr.alarm(1, 1000, 1, function()
	if wifi.sta.getip()== nil then
		print("IP unavaiable, Waiting...")
	else
		tmr.stop(1)
		broadcast = wifi.sta.getbroadcast()
		register_json = "{ \"title\" : \"" .. deviceName .. "\", \"ip\" : \"" .. wifi.sta.getip() .. "\", \"port\" : " .. 5683 .. ", \"mac\" : \"" .. wifi.ap.getmac() .. "\", \"version\" : \"" .. ver .. "\"}"
		cc:post(coap.CON, "coap://" .. broadcast .. ":5683/register", register_json)
 	end
end)

tmr.alarm(2, 5000, 1, function()
	if UUID == nil or server_ip == nil then
		print("device not active, Waiting...")
	else
		tmr.stop(2)
		active_json = "{ \"uuid\" : \"" .. UUID .. "\"}"
		cc:post(coap.CON, service_notify_active, active_json)
 	end
end)

cs=coap.Server()
cs:listen(5683)
-- server post to device functions
cs:func("uuid")
cs:func("serverAddress")
function uuid(payload)
	print(payload)
	UUID=payload
	respond = "OK"
	return respond
end
function serverAddress(payload)
	server_ip = payload
	service_proximity = server_ip .. "proximity"
	service_register = server_ip .. "register"
	service_notify_active = server_ip .. "active"
	respond = "OK"
	return respond
end

cs:func("proximity_start")
cs:func("proximity_stop")
function proximity_start(payload)
    tmr.start(proximity_detector_timer_id)
    proximity_detector_status = true
    respond = "OK"
    return respond
end
function proximity_stop(payload)
    tmr.stop(proximity_detector_timer_id)
    proximity_detector_status = false
    respond = "OK"
    return respond
end
cs:func("proximity_color_active")
function proximity_color_active(payload)
    proximity_detector_color_active = payload
    respond = "OK"
    return respond
end
cs:func("touch_start")
cs:func("touch_stop")
function touch_start(payload)
--	tmr.start(proximity_detector_timer_id)
--	proximity_detector_status = true

	gpio.mode(tmr_touch_port,gpio.INT,gpio.PULLUP)
	gpio.trig(tmr_touch_port, "down",touch_fun)
	write_reg(0x50,0x00,0x00)
	respond = "OK"
	return respond
end
function touch_stop(payload)
--	tmr.stop(proximity_detector_timer_id)
	gpio.mode(tmr_touch_port, gpio.INPUT)
--	proximity_detector_status = false
	respond = "OK"
	return respond
end

function hsvToRgb(h, s, v, a)
    local r, g, b

    local i = math.floor(h * 6);
    local f = h * 6 - i;
    local p = v * (1 - s);
    local q = v * (1 - f * s);
    local t = v * (1 - (1 - f) * s);

    i = i % 6

    if i == 0 then r, g, b = v, t, p
    elseif i == 1 then r, g, b = q, v, p
    elseif i == 2 then r, g, b = p, v, t
    elseif i == 3 then r, g, b = p, q, v
    elseif i == 4 then r, g, b = t, p, v
    elseif i == 5 then r, g, b = v, p, q
	end
	if r > 1 then r=1 end
	if g > 1 then g=1 end
	if b > 1 then b=1 end
	if a > 1 then a=1 end

    return r * 1023, g * 1023, b * 1023, a * 1023
end
-- Get the mean value of a table
function mean( t )
	local sum = 0
	local count= 0

	for k,v in pairs(t) do
		sum = sum + v
		count = count + 1
	end

	return (sum / count)
end

array_proximity = {}
array_size = 5
for i= 1, array_size do
	array_proximity[i] = 0
end
array_id = 1


tmr.register(proximity_detector_timer_id, 50, 1, function()
	if UUID == nil then
		print("UUID unavaiable, need to restart...")
		node.restart() 
	else
		reg_val =string.byte(read_reg(0x50,0x10))
		touch_val = bit.band(reg_val,127)-bit.band(reg_val,128)
		array_proximity[array_id] = touch_val
		array_id = array_id +1
		if array_id > array_size then
			array_id = 1
		end
		mean_val = mean(array_proximity)
		touch_json = "{ \"uuid\" : \"" .. UUID .. "\", \"proximity\" : " .. mean_val .. "}"

		if proximity_detector_color_active == "1" then
			if mean_val>4 then r,g,b=hsvToRgb(mean_val/127,1,(mean_val-4)/60,1)
			else b=0
				r=0
				g=0
			end
			pwm.setduty(G,g)
			pwm.setduty(R,r)
			pwm.setduty(B,b)
		else
			cc:post(coap.NON, service_proximity, touch_json)
			pwm.setduty(G,0)
			pwm.setduty(R,0)
			pwm.setduty(B,0)

		end
	end
end)

cs:func("set_r")
cs:func("set_g")
cs:func("set_b")
cs:func("sensitivity")
function set_r(payload)
	pwm.setduty(R, payload)
	global_r=payload
	respond = "OK"
	return respond
end
function set_g(payload)
	pwm.setduty(G, payload)
	global_g=payload
	respond = "OK"
	return respond
end
function set_b(payload)
	pwm.setduty(B, payload)
	global_b=payload
	respond = "OK"
	return respond
end
function sensitivity(payload)
	sensitivity_value = payload
    node.input("write_reg(0x50,0x1F,0x" .. payload .. "F)")
	respond = "OK"
	return respond
end

cs:func("status")
function status(payload)
	status_json = "{\"uuid\":\""..UUID.."\",\"detector\":"..tostring(proximity_detector_status)..",\"sensitivity\":"..sensitivity_value..",\"r\":"..pwm.getduty(R)..",\"g\":"..pwm.getduty(G)..",\"b\":"..pwm.getduty(B).."}"
	respond = status_json
	return respond
end

cs:func("calibrate")
function calibrate(payload)
	write_reg(0x50,0x26,0x01)
	respond = "OK"
	return respond
end

cs:func("requestRefersh")
function requestRefersh(payload)
	tmr.start(2)
	server_ip = payload
	service_proximity = server_ip .. "proximity"
	service_register = server_ip .. "register"

	register_json = "{ \"title\" : \"" .. deviceName .. "\", \"ip\" : \"" .. wifi.sta.getip() .. "\", \"port\" : " .. 5683 .. ", \"mac\" : \"" .. wifi.ap.getmac() .. "\", \"version\" : \"" .. ver .. "\"}"
	cc:post(coap.CON, service_register, register_json)
	respond = "OK"
	return respond
end

isActive=1
cs:var("isActive")

tmr_touch_port=5
a = {}
is4=0
time = 0
div_time = 0
write_reg(0x50,0x22,0xa7)
write_reg(0x50,0x27,0x01)
write_reg(0x50,0x21,0x01)


function touch_fun(level)
	div_time=tmr.now()-time
	time = tmr.now()

	if div_time <310000 then
		table.insert(a, div_time)
		is4 = is4+1
	else is4=0 a={} end
	if is4==4 then
		if diode == 0 then
			if global_r=='0' and global_g=='0' and global_b=='0' then
				pwm.setduty(R,0)
				pwm.setduty(G,512)
				pwm.setduty(B,0)
			else
				pwm.setduty(R,global_r)
				pwm.setduty(G,global_g)
				pwm.setduty(B,global_b)
			end
			diode =1
		else
			pwm.setduty(R,0)
			pwm.setduty(G,0)
			pwm.setduty(B,0)
			diode =0
		end
		is4=0
		a={}
	end
	tmr.delay(80000)
	write_reg(0x50,0x00,0x00)
end