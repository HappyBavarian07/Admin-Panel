import {serverUrlForAPI} from "/js/config.js";
export function checkPermission(permission) {
    // Send an API request to the server to check if the user has the required permission
    return fetch(serverUrlForAPI + '/adminpanel/secure-api/check-permission?permission=' + encodeURIComponent(permission), {
        method: 'GET',
    })
        .then(response => {
            if(!response.ok) {
                // Return false and tell the User that he is not authorized to access this page
                alert('You are not authorized to access this feature (' + permission + '). If you believe this is an error, please contact your system administrator.');
                return null;
            }
            return response.text()
        })
        .then(data => {
            if(data === null) return false;
            // Return if the user has the required permission
            if(data === 'Unauthorized') {
                // Return false and tell the User that he is not authorized to access this page
                alert('You are not authorized to access this feature (' + permission + '). If you believe this is an error, please contact your system administrator.');
                return false;
            }
            if(data === 'true') {
                return true;
            } else {
                // Return false and tell the User that he is not authorized to access this page
                alert('You are not authorized to access this feature (' + permission + '). If you believe this is an error, please contact your system administrator.');
                return false;
            }
        });
}