Hap - An Android news app without GUI
===

This app

- Uses no GUI for presenting content and navigating through it
  - Text-to-speech and on-demand audio are used instead for feedback and presentation 
- Uses the device's motion sensor (angular speed) for input
  - The user "pilot" themself egocentrically through a web of content
- Uses an evolving REST client
  - Talks to [NPR's API][npr_api_url] for news content
- Adopts a content-first approach to architecture
  - Navigation is from content collection/item to content collection/item

[npr_api_url]: http://www.npr.org/api/index

