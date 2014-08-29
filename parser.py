from bs4 import BeautifulSoup
import urllib2
import json
import re

HOME = 'http://frpbc.ca'

# Return html given a link
def get_html(link):
    return urllib2.urlopen(link).read()

# Returns list of links from html page and class attribute
def get_links(page_html, class_attr):
    soup = BeautifulSoup(page_html)
    region_list = soup.find('ul', {'class' : class_attr})

    list_links = []
    for link in region_list.find_all('li'):
        path = link.find('a')['href']
        list_links.append("%s%s" % (HOME, path))

    return list_links

def collect_agency_info(agency_html):
    soup = BeautifulSoup(agency_html)

    name = soup.find('h3', {'class' : 'member-agency'}).string
    phoneNumber = soup.find('p', {'class' : 'member-phone-number'}).contents[2]
    website = soup.find('p', {'class' : 'member-website'}).find('a')['href']
    address = re.match(r'Map of (.*)', soup.find_all('noscript')[1].find('img')['alt'])
    latlong = re.search(r'var latlng = new google\.maps\.LatLng\((.*), (.*)\);', agency_html)
    delivering_agency = soup.find('p', {'class' : 'member-delivering-agency'}).contents[2]
    hours = soup.find('table', {'style' : 'width:240px;'}).find_all('tr')

    hourlist = []
    for hour in hours:
        hourlist.append(hour.find('th').string + hour.find('td').string)

    poi = {
            "name" : name,
            "agency" : delivering_agency,
            "phoneNumber" : phoneNumber,
            "website" : website,
            "location" : { 
                "address" : address.group(1),
                "latitude" : latlong.group(1),
                "longitude" : latlong.group(2)
            },
            "hoursOfOperation" : hourlist
        }

    return poi

# Given community returns links to agencies
def get_agency_links(community_html):
    soup = BeautifulSoup(community_html)
    agency_urls = soup.find('b', {'class' : 'member-agency'})

    links = []
    for link in agency_urls.find_all('a'):
        path = link['href']
        links.append("%s%s" % (HOME, path))
    
    return links

if __name__ == '__main__':

    # Get html of directory page
    dir_html = get_html(HOME + '/dir/')
    region_links = get_links(dir_html, 'regionlist')
    agency_list = []

    for region in region_links:
        region_html = get_html(region)
        comm_links = get_links(region_html, 'communities')

        for community in comm_links:
            community_html = get_html(community)
            agency_links = get_agency_links(community_html)

            for link in agency_links:
                try:
                    agency_html = get_html(link)
                    agency_list.append(collect_agency_info(agency_html))
                except urllib2.HTTPError, e:
                    print "500 Error at: %s" % link

    f = open('stuff.txt', 'w+')
    f.write(json.dumps(agency_list, indent=4))