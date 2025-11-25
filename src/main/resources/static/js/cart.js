// cart.js

// Add product to cart
window.addToCart = async function(productId, size = null, quantity = 1) {
    if (!size) {
        alert('Please select a size before adding to cart.');
        return;
    }

    try {
        const response = await fetch('/cart/add', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ productId, size: size.trim().toUpperCase(), quantity })
        });
        const data = await response.json();

        if (data.success) {
            updateCartBadge();
            showCartToast();  // test
//            alert('Added to cart!');
        } else {
            alert(data.message || 'Failed to add to cart.');
        }
    } catch (err) {
        console.error(err);
        alert('Failed to add to cart.');
    }
};

// Update cart badge in header
window.updateCartBadge = async function() {
    try {
        const res = await fetch('/cart/items');
        const cart = await res.json();
        const badge = document.getElementById('cart-badge');
        if (badge) badge.textContent = cart.items ? cart.items.length : 0;
    } catch (err) {
        console.error('Failed to update cart badge', err);
    }
};




// test
// ✅ Show Bootstrap toast for Add to Cart
window.showCartToast = function() {
    const toastEl = document.getElementById('cartToast');
    if (!toastEl) return;

    const toast = new bootstrap.Toast(toastEl);
    toast.show();
};




// Run badge update on page load
window.addEventListener('DOMContentLoaded', updateCartBadge);



















//// cart.js
//
//// Add product to cart
//window.addToCart = async function(productId, size = null, quantity = 1) {
//    try {
//        const response = await fetch('/cart/add', {
//            method: 'POST',
//            headers: {'Content-Type': 'application/json'},
//            body: JSON.stringify({ productId, size, quantity })
//        });
//        const data = await response.json();
//
//        if (data.success) {
//            updateCartBadge();
//            alert('Added to cart!');
//        } else {
//            alert(data.message);
//        }
//    } catch (err) {
//        console.error(err);
//        alert('Failed to add to cart.');
//    }
//};
//
//// Update cart badge in header
//window.updateCartBadge = async function() {
//    try {
//        const res = await fetch('/cart/items');
//        const cart = await res.json();
//        const badge = document.getElementById('cart-badge');
//        if (badge) badge.textContent = cart.items ? cart.items.length : 0;
//    } catch (err) {
//        console.error('Failed to update cart badge', err);
//    }
//};
//
//// Run badge update on page load
//window.addEventListener('DOMContentLoaded', updateCartBadge);
