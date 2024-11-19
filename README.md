# pdf-xfa-form-patcher
An example of how to edit a PDF XFA form using iText  
In this fictional example we're going to add a button 'Add Row Above' next to the 'Delete Row' rows, which will add a new row above the current one. See the screenshot below comparing the original and the patched form.
* Load the PDF (`FDA-2253_Dyn_Sec_Ext_R8 09-28-2021.pdf`)
* Extact the XFA form XML content (`original.xml`)
* Analyze it manually 
* Apply AdHoc edits (applied by the Java code and saved into `patched.xml` for comparison)
* Save the XML back into PDF (`FDA-2253_Dyn_Sec_Ext_R8 09-28-2021-patched.pdf`)


<img width="1121" alt="Screenshot 2023-12-17 at 22 57 49" src="https://github.com/alex-sc/pdf-xfa-form-patcher/assets/5649729/8943e90a-937c-41ad-93d1-c949ebb06d08">
