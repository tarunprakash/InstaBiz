## search if name is in database
def binsearch(array, target):
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
  ## gets name, phone, email, site, address, company
  names = open("sortednames.txt","r").readlines()
  text = [x.split() for x in text]
  info = {}
 
  for i in text:
    if "@" in i[0]: ## if the string has an @ sign, then its an email
      info["email"] = "".join(i)

    elif any(x.isdigit() for x in map(str,i)): # if there is a number
      if any(x.isalpha() for x in map(str,i)): # if there is a letter as well, then address
        info["address"] = " ".join(i)
      elif (any(x.isdigit() for y in i for x in y) and any("p" in x for x in i)):
        temp = "".join(i)
        info["phone"] = "".join([x for x in temp if x.isdigit()])
 
    elif "." in i[0]: # if there is a . , then classified as a website
      info["site"] = "".join(i)

    elif binsearch(names, i[0].lower()): ## if string is in names db then classified as a name
      if "name" not in info:
        info["name"] = " ".join(i)

    elif "company" not in info:
      info["company"] = " ".join(i) 

  return info



