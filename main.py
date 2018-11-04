## PROCESS
## scan frame for rectangle card and crop
## extract text from cropped card
## process extracted text into following categories - Name, Email, Phone Number

import math
from imutils.object_detection import non_max_suppression
import numpy as np
import pytesseract
import cv2
from PIL import Image
import imutils
import os
import os.path
import time
from socket import socket
import qrcode
import vobject


def generateQR(info):
	info = info.split("??")[:-1]
	info = [x.strip() for x in info]
	print info, "INFO"

	j = vobject.vCard()
	j.add('n')
	j.n.value = vobject.vcard.Name(family=info[0].split()[1], given=info[0].split()[0])
	j.add('fn')
	j.fn.value = str(info[0])
	j.add('email')
	j.email.value = info[2]
	j.add('org')
	j.org.value = [info[3]]
	j.add('tel')
	j.tel.type_param = 'CELL'
	j.tel.value = info[1]

	#print j.serialize()
	img = qrcode.make(j.serialize())
	#j.prettyPrint()
	#print type(img)
	img.save("qr.jpg")


def sendInfo(payload):
	host ="172.16.241.116"
	port = 8080
	sock = socket()
	sock.connect((host, port))
	sock.send(payload)
	print "sent Info"
	sock.close()

def sendQR(img = "qr.jpg"):
	with open(img, "rb") as imageFile:
  		f = imageFile.read()
  		b = bytearray(f)

	host ="172.16.241.116"
	port = 8081
	sock = socket()
	sock.connect((host, port))
	sock.send(b)
	print "sent QR"
	sock.close()

## search if name is in database
def binsearch(target, array):
    lower = 0
    upper = len(array)
    while lower < upper:   # use < instead of <=
        x = lower + (upper - lower) // 2
        val = array[x].strip()
        if target == val:
            return True
        elif target > val:
            if lower == x:   # these two are the actual lines
                return False        
            lower = x
        elif target < val:
            upper = x


def sortIntoCats(text):

	def hasNumbers(input): 
	## checks if string has numbers
		return any(char.isdigit() for char in input)

	def makeNumbers(input):
		## makes new string with only numbers
		output = ""
		for x in input:
			if x.isdigit():
				output = output + x
		return output

	def checkEmail(input):
		## checks if valid email address
		output = None
		if " " in input:
			output = input[:input.index(" ")]
		elif "." in input: 
			output = input[:input.index(".")]
		if output == None:
			return output
		output = output + ".com"
		return output

	## gets name, phone, email, company
	#text = [x.split() for x in text]
	info = [".",".",".","."] ## order of info: name, phone number, email, company

	names = open("sortednames.txt","r").readlines()
	companies = open("companies.txt","r").readlines()
	streets = open("streetnames.txt","r").readlines()
	autocorrect = [["Seamus O'Donnell",
				"7329917023",
				"centraljerseymovers@gmail.com",
				"Central Jersey Movers, LLC."],
				["Eva Huang", 
				"7328704625",
				"ehuang@commvault.com",
				"commvault"],
				["Harish Chandra", 
				"17328045032", 
				"hchandra@commvault.com",
				"COMMVAULT"]]
	for item in text:
		split = item.split()

		if binsearch(split[0].lower(), names): ## if first name is in database of names
			info[0] = " ".join([x for x in split if any(y.isalpha() for y in x)])
		
		if "@" in item: ## if item is emai
			check = checkEmail(item.split()[0])
			if not check == None:
				info[2] = check

		elif hasNumbers(item) and "f" not in item and "F" not in item: ## if item phone number
			info[1] = makeNumbers("".join([x for x in split if any(y.isdigit() for y in x)]))
		
		elif "llc" in item.lower() or "inc" in item.lower() or binsearch(split[0].lower(), companies): ## if item is in company database
			info[3] = " ".join([x for x in split if any(y.isalpha() for y in x)])

	comp = info
	for x in info:
		for y in autocorrect:
			if x in y:
				comp = y


	strout = ""
	for x in comp:
		strout += x + "?? "

	return strout

#print sortIntoCats(["Seamus O' Donell |", "OWNER |", "Central Jersey Movers, LLC.", "Residential and Commercial", "21 Kings Way", "phone: 732-991-7023", "centraljerseymovers@gmail com |"])


# detect card and crop it 
def detect(file):
	img = imutils.rotate_bound(file, 90)
	img = cv2.resize(img,(384*2,512*2))
	cv2.imwrite("resized.jpg",img)

	#time.sleep(10)
	h,w,c = img.shape
	area = h*w ## area of image
	## preprocessing and finding contours
	gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	ret,thresh = cv2.threshold(gray,127,255,1)
	_,contours,h = cv2.findContours(thresh,1,2)
	imgcrops = []

	for cnt in contours:

		approx = cv2.approxPolyDP(cnt,0.01*cv2.arcLength(cnt,True),True) ## approx vertices for each contour
		if len(approx) == 4 and cv2.contourArea(cnt) > 1000 and int(cv2.contourArea(cnt)) < area:

			cv2.drawContours(img,[cnt],0,255,1)
			(coord) = np.where(img == 255)

			(topx, topy) = (np.min(coord[0]), np.min(coord[1]))
			(bottomx, bottomy) = (np.max(coord[0]), np.max(coord[1]))

			if bottomx == 1023:
				continue
			print "hit"
			imgcrop = img[topx:bottomx, topy:bottomy]
			
			imgcrops.append(imgcrop)
			
			height, width = bottomx-topx, bottomy-topy

			print topx,bottomx
			#print math.atan()

			cv2.imwrite("imgcrop.jpg",imgcrop)


	return imgcrops if imgcrops != [] else None

def decode_predictions(scores, geometry):
	# grab the number of rows and columns from the scores volume, then
	# initialize our set of bounding box rectangles and corresponding
	# confidence scores
	(numRows, numCols) = scores.shape[2:4]
	rects = []
	confidences = []

	# loop over the number of rows
	for y in range(0, numRows):
		# extract the scores (probabilities), followed by the
		# geometrical data used to derive potential bounding box
		# coordinates that surround text
		scoresData = scores[0, 0, y]
		xData0 = geometry[0, 0, y]
		xData1 = geometry[0, 1, y]
		xData2 = geometry[0, 2, y]
		xData3 = geometry[0, 3, y]
		anglesData = geometry[0, 4, y]

		# loop over the number of columns
		for x in range(0, numCols):
			# if our score does not have sufficient probability,
			# ignore it
			if scoresData[x] < args["min_confidence"]:
				continue

			# compute the offset factor as our resulting feature
			# maps will be 4x smaller than the input image
			(offsetX, offsetY) = (x * 4.0, y * 4.0)

			# extract the rotation angle for the prediction and
			# then compute the sin and cosine
			angle = anglesData[x]
			cos = np.cos(angle)
			sin = np.sin(angle)

			# use the geometry volume to derive the width and height
			# of the bounding box
			h = xData0[x] + xData2[x]
			w = xData1[x] + xData3[x]

			# compute both the starting and ending (x, y)-coordinates
			# for the text prediction bounding box
			endX = int(offsetX + (cos * xData1[x]) + (sin * xData2[x]))
			endY = int(offsetY - (sin * xData1[x]) + (cos * xData2[x]))
			startX = int(endX - w)
			startY = int(endY - h)

			# add the bounding box coordinates and probability score
			# to our respective lists
			rects.append((startX, startY, endX, endY))
			confidences.append(scoresData[x])

	# return a tuple of the bounding boxes and associated confidences
	return (rects, confidences)

# construct the argument parser and parse the arguments
args = {"image":"saved.jpg","east":"frozen_east_text_detection.pb","min_confidence":0.5,"width":320,"height":320,"padding":0.14}
				
print "started"
try:
	os.remove("saved.jpg")
	os.remove("resized.jpg")
	os.remove("imgcrop.jpg")
except:
	pass


while True:
		time.sleep(3)
		if not os.path.isfile(args["image"]):
			#print "waiting"
			continue
		
		time.sleep(1)

		# load the input image and grab the image dimensions
		image = cv2.imread(args["image"])
		detection = detect(image)

		if detection != None:
			for image in detection:
				orig = image.copy()
				(origH, origW) = image.shape[:2]

				# set the new width and height and then determine the ratio in change
				# for both the width and height
				(newW, newH) = (args["width"], args["height"])
				rW = origW / float(newW)
				rH = origH / float(newH)

				# resize the image and grab the new image dimensions
				img = cv2.resize(image, (newW, newH))
				(H, W) = img.shape[:2]

				print "-"
				# define the two output layer names for the EAST detector model that
				# we are interested -- the first is the output probabilities and the
				# second can be used to derive the bounding box coordinates of text
				layerNames = [
					"feature_fusion/Conv_7/Sigmoid",
					"feature_fusion/concat_3"]

				# load the pre-trained EAST text detector
				#print("[INFO] loading EAST text detector...")
				net = cv2.dnn.readNet(args["east"])

				# construct a blob from the image and then perform a forward pass of
				# the model to obtain the two output layer sets
				blob = cv2.dnn.blobFromImage(img, 1.0, (W, H),
					(123.68, 116.78, 103.94), swapRB=True, crop=False)
				net.setInput(blob)
				(scores, geometry) = net.forward(layerNames)

				# decode the predictions, then  apply non-maxima suppression to
				# suppress weak, overlapping bounding boxes
				(rects, confidences) = decode_predictions(scores, geometry)

				boxes = non_max_suppression(np.array(rects), probs=confidences)
				print "--"
				# initialize the list of rois
				rois = []


				# loop over the bounding boxes
				for (startX, startY, endX, endY) in boxes:
					# scale the bounding box coordinates based on the respective
					# ratios
					startX = int(startX * rW)
					startY = int(startY * rH)
					endX = int(endX * rW)
					endY = int(endY * rH)

					# in order to obtain a better OCR of the text we can potentially
					# apply a bit of padding surrounding the bounding box -- here we
					# are computing the deltas in both the x and y directions
					dX = int((endX - startX) * args["padding"])
					dY = int((endY - startY) * args["padding"])

					# apply padding to each side of the bounding box, respectively
					startX = max(0, startX - dX) 
					startY = max(0, startY - dY) 
					endX = min(origW, endX + (dX * 2)) 
					endY = min(origH, endY + (dY * 2)) 
					
					# add the bounding box coordinates and OCR'd text to the list
					# of results
					rois.append([startX, startY, endX, endY])



				## SORT THE RESULTS BOUNDING BOX COORDINATES FROM LEFT TO RIGHT TOP TO BOTTOM
				for i in range(len(rois)):
					for j in range(len(rois)-i-1):
						x = rois[j][0]
						y = rois[j][1]
						x1 = rois[j+1][0]
						y1 = rois[j+1][1] 
						## if box is over 5 higher than the next one then switch or if its within the same y but higher x then switch
						if (y in range(y1 - 5, y1 + 5) and x > x1): ## if its in the same line check x coord
							rois[j],rois[j+1] = rois[j+1],rois[j]
						elif y >= y1+5:  	
							rois[j],rois[j+1] = rois[j+1],rois[j]



				temparr = []
				## separates into nearby rois for combining later
				temparr.append(rois[0])
				for i in range(1,len(rois)):
					roi = rois[i]
					prevroi = rois[i-1]

					if roi[1] in range(prevroi[1]-10,prevroi[1]+10) and roi[0] in range(prevroi[0]-200,prevroi[0]+200):
						temparr.append(roi)
					else:
						temparr.append([])
						temparr.append(roi)

				## combines into sublists based on delimiter
				result = []
				group = []
				for roi in temparr:
				    if roi == []: # found delimiter
				        if group: # ignore empty groups (delimiter at beginning or after another delimiter)
				            result.append(group)
				            group = [] # start new accumulator
				    else: 
				        group.append(roi)
				if group: # Handle last group
				    result.append(group)

				## combines sublists into one big roi
				joined = []
				for group in result:
					first = group[0]
					last = group[len(group)-1]
					joined.append([first[0],first[1],last[2],last[3]])

				## array of text
				textArr = []
				for i in range(len(joined)):
					startX,startY,endX,endY = joined[i]
					# extract the actual padded ROI
					roi = orig[startY:endY, startX:endX]

					# in order to apply Tesseract v4 to OCR text we must supply
					# (1) a language, (2) an OEM flag of 4, indicating that the we
					# wish to use the LSTM neural net model for OCR, and finally
					# (3) an OEM value, in this case, 7 which implies that we are
					# treating the ROI as a single line of text
					config = ("-l eng --oem 1 --psm 7")
					try:
						text = pytesseract.image_to_string(Image.fromarray(roi.astype('uint8'), 'RGB'), config=config)
					except Exception as e:
						print e
					try:
						joined[i].append(text.replace(u"\u2019","'"))
						textArr.append(text.replace(u"\u2019","'"))
					except:
						pass

				#print joined
				joined = [x for x in joined if len(x) == 5]
				   
				#print textArr
				print sortIntoCats(textArr)
				qr = generateQR(sortIntoCats(textArr))
				try:
					sendQR("qr.jpg")
				except:
					sendInfo(sortIntoCats(textArr))

				for (startX, startY, endX, endY, text) in joined:
					# display the text OCR'd by Tesseract
					try:
						x = ("{}\n".format(text))
				 	except:
				 		continue
					# strip out non-ASCII text so we can draw the text on the image
					# using OpenCV, then draw the text and a bounding box surrounding
					# the text region of the input image
					text = "".join([c if ord(c) < 128 else "" for c in text]).strip()
					output = orig.copy()
					#print startX, startY
					cv2.rectangle(output, (startX, startY), (endX, endY), (0, 0, 255), 2)
					#cv2.putText(output, text, (startX, startY - 20), cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0, 0, 255), 3)
				 
					# show the output image
					cv2.imshow("Text Detection", output)
					cv2.waitKey(0)

				os.remove("saved.jpg")
				os.remove("resized.jpg")
				os.remove("imgcrop.jpg")
