// product.js

// product.js

// Switch main image when thumbnail clicked
function showMainImage(img) {
    const mainImg = document.getElementById('mainProductImage');
    if (mainImg) mainImg.src = img.src;
}

// Track stock & cart
const MAX_QTY = 5; // max units per product
let sizesStock = {}; // stock per size
let userCart = [];   // current cart items

// Initialize size buttons and stock
function initSizes() {
    // 1. Get current cart items
    fetch('/cart/items')
        .then(res => res.json())
        .then(cartData => {
            userCart = cartData.items || [];

            // 2. Store stock from data attributes
            document.querySelectorAll('.btn-size').forEach(btn => {
                const size = btn.dataset.size.toUpperCase();
                sizesStock[size] = parseInt(btn.dataset.stock);
            });

            updateSizeButtons();
        });

    // 3. Attach click handlers
    document.querySelectorAll('.btn-size').forEach(btn => {
        btn.addEventListener('click', () => selectSize(btn.dataset.size));
    });
}


function updateStockLabel() {
    const stockLabel = document.getElementById('stockLabel');
        if (!stockLabel) return;

        let hasStock = false;

        for (const size in sizesStock) {
            const stock = sizesStock[size];
            const cartItem = userCart.find(ci => ci.fitSize.toUpperCase() === size);
            const cartQty = cartItem ? cartItem.quantity : 0;

            // Remaining stock for this size
            const remaining = stock - cartQty;

            // If even one size has > 0 remaining, mark as available
            if (remaining > 0) {
                hasStock = true;
                break;
            }
        }

        if (hasStock) {
            stockLabel.textContent = 'In Stock';
            stockLabel.className = 'text-success fw-bold';
        } else {
            stockLabel.textContent = 'Out of Stock';
            stockLabel.className = 'text-warning fw-bold';
        }
}

// Update size buttons based on stock & cart
function updateSizeButtons() {
    document.querySelectorAll('.btn-size').forEach(btn => {
        const size = btn.dataset.size.toUpperCase();
        const stock = sizesStock[size] || 0;

        const cartItem = userCart.find(ci => ci.fitSize.toUpperCase() === size);
        const cartQty = cartItem ? cartItem.quantity : 0;

        const remainingStock = stock - cartQty;
        const disableBtn = remainingStock <= 0 || cartQty >= MAX_QTY;

        btn.disabled = disableBtn;
        btn.classList.toggle('btn-outline-primary', !disableBtn);
        btn.classList.toggle('btn-primary', btn.classList.contains('btn-primary') && !disableBtn);
        btn.classList.toggle('btn-secondary', disableBtn);
        btn.classList.toggle('disabled', disableBtn);
    });

    updateStockLabel(); // update label dynamically
}

// Highlight selected size
function selectSize(size) {
    const selectedSizeInput = document.getElementById('selectedSize');
    size = size.toUpperCase().trim();
    selectedSizeInput.value = size;

    document.querySelectorAll('.btn-size').forEach(btn => {
        btn.classList.remove('btn-primary');
        btn.classList.add('btn-outline-primary');
    });

    const btn = Array.from(document.querySelectorAll('.btn-size'))
                     .find(b => b.dataset.size.toUpperCase() === size);
    if(btn) btn.classList.add('btn-primary');

    const addBtn = document.getElementById('addToCartBtn');
    if(addBtn) addBtn.disabled = !size;
}

// Update local cart & refresh buttons after adding
function handleCartUpdate(size) {
    size = size.toUpperCase();
    const cartItem = userCart.find(ci => ci.fitSize.toUpperCase() === size);

    if (cartItem) {
        cartItem.quantity += 1;
    } else {
        userCart.push({ fitSize: size, quantity: 1 });
    }

    // Reduce the stock locally
    if (sizesStock[size] > 0) {
        sizesStock[size] -= 1;
    }

    updateSizeButtons();
}

// Add to Cart button logic
window.addEventListener('DOMContentLoaded', () => {
    initSizes();

    const addBtn = document.getElementById('addToCartBtn');
    const selectedSizeInput = document.getElementById('selectedSize');

    if(addBtn) {
        addBtn.addEventListener('click', () => {
            const productId = document.getElementById('productId').value;
            const size = selectedSizeInput.value.trim().toUpperCase();

            if(!size) { alert('Please select a size before adding to cart.'); return; }

            if(window.addToCart) {
                window.addToCart(productId, size, 1).then(() => {
                    handleCartUpdate(size); // disable size if stock exhausted
                    updateSizeButtons();
                });
            } else {
                alert('Add to Cart function not available.');
            }
        });
    }
});











//function updateStockLabel() {
//    let totalRemaining = 0;
//
//    for (const size in sizesStock) {
//        const cartItem = userCart.find(ci => ci.fitSize.toUpperCase() === size);
//        const cartQty = cartItem ? cartItem.quantity : 0;
//        totalRemaining += (sizesStock[size] - cartQty);
//    }
//
//    const stockLabel = document.getElementById('stockLabel');
//    if (!stockLabel) return;
//
//    if (totalRemaining <= 0) {
//        stockLabel.textContent = 'Out of Stock';
//        stockLabel.className = 'text-warning fw-bold';
//    } else {
//        stockLabel.textContent = 'In Stock';
//        stockLabel.className = 'text-success fw-bold';
//    }
//}









// Update local cart & refresh buttons after adding
//function handleCartUpdate(size) {
//    const cartItem = userCart.find(ci => ci.fitSize.toUpperCase() === size);
//    if(cartItem) {
//        cartItem.quantity += 1;
//    } else {
//        userCart.push({ fitSize: size, quantity: 1 });
//    }
//    updateSizeButtons();
//}














//// Switch main image when thumbnail clicked
//function showMainImage(img) {
//    const mainImg = document.getElementById('mainProductImage');
//    if (mainImg) mainImg.src = img.src;
//}
//
//// Select a product size
//function selectSize(size) {
//    const selectedSizeInput = document.getElementById('selectedSize');
//    size = size.toUpperCase().trim();  // normalize to uppercase
//    selectedSizeInput.value = size;
//
//    // Reset all buttons
//    document.querySelectorAll('.btn-size').forEach(btn => {
//        btn.classList.remove('btn-primary');
//        btn.classList.add('btn-outline-primary');
//    });
//
//    // Highlight selected button
//    document.querySelectorAll('.btn-size').forEach(btn => {
//        if (btn.dataset.size.toUpperCase() === size) {
//            btn.classList.remove('btn-outline-primary');
//            btn.classList.add('btn-primary');
//        }
//    });
//
//    // Enable Add to Cart button
//    const addBtn = document.getElementById('addToCartBtn');
//    if (addBtn) addBtn.disabled = !size;
//}
//
//// Add to Cart button logic
//window.addEventListener('DOMContentLoaded', () => {
//    const addBtn = document.getElementById('addToCartBtn');
//    const selectedSizeInput = document.getElementById('selectedSize');
//
//     // Attach size button click handlers
//        document.querySelectorAll('.btn-size').forEach(btn => {
//            btn.addEventListener('click', () => {
//                selectSize(btn.dataset.size);
//            });
//        });
//
//    if (addBtn) {
//        addBtn.addEventListener('click', () => {
//            const productId = document.getElementById('productId').value;
//            let size = selectedSizeInput.value.trim().toUpperCase();
//
//            if (!size) {
//                alert('Please select a size before adding to cart.');
//                return;
//            }
//
//            console.log("Selected Size before addToCart:", size);
//
//            // Call cart.js function
//            if (window.addToCart) {
//                window.addToCart(productId, size, 1);
//            } else {
//                alert('Add to Cart function not available.');
//            }
//        });
//    }
//});
//
//
//
//
//

















//// product.js
//
//// Switch main image when thumbnail clicked
//function showMainImage(img) {
//    const mainImg = document.getElementById('mainProductImage');
//    if (mainImg) mainImg.src = img.src;
//}
//
//// Select a product size
//function selectSize(size) {
//    const selectedSizeInput = document.getElementById('selectedSize');
//    selectedSizeInput.value = size.toUpperCase().trim();
//
//    const buttons = document.querySelectorAll('.btn-size');
//    buttons.forEach(btn => btn.classList.remove('btn-primary'));
//    buttons.forEach(btn => {
//        if (btn.dataset.size.toUpperCase() === size.toUpperCase()) btn.classList.add('btn-primary');
//    });
//
//    const addBtn = document.getElementById('addToCartBtn');
//    if (addBtn) addBtn.disabled = !size;
//}
//
//// Add to Cart button logic
//window.addEventListener('DOMContentLoaded', () => {
//    const addBtn = document.getElementById('addToCartBtn');
//    const selectedSizeInput = document.getElementById('selectedSize');
//
//    if (addBtn) {
//        addBtn.addEventListener('click', () => {
//            const productId = document.getElementById('productId').value;
//            let size = selectedSizeInput.value;
//
//            if (!size) {
//                alert('Please select a size before adding to cart.');
//                return;
//            }
//
//
//
//
//            selectedSizeInput.value = size.toUpperCase().trim();
//            console.log("Selected Size before addToCart:", selectedSizeInput.value);
//
//
//
//
//
//            // Call cart.js function
//            if (window.addToCart) {
//                window.addToCart(productId, size, 1);
//            } else {
//                alert('Add to Cart function not available.');
//            }
//        });
//    }
//});
//















// if (addBtn) {
//        addBtn.addEventListener('click', () => {
//            const productId = document.getElementById('productId').value;
//            const size = selectedSizeInput.value;
//
//            if (!size && document.querySelectorAll('.btn-size').length) {
//                alert('Please select a size before adding to cart.');
//                return;
//            }
//
//            // Call cart.js function
//            if (window.addToCart) {
//                window.addToCart(productId, size, 1);
//            } else {
//                alert('Add to Cart function not available.');
//            }
//        });






//// product.js
//
//// Switch main image when thumbnail clicked
//function showMainImage(img) {
//    const mainImg = document.getElementById('mainProductImage');
//    if (mainImg) mainImg.src = img.src;
//}
//
//// Select a product size
//function selectSize(size) {
//    const selectedSizeInput = document.getElementById('selectedSize');
//    selectedSizeInput.value = size;
//
//    const buttons = document.querySelectorAll('.btn-size');
//    buttons.forEach(btn => btn.classList.remove('btn-primary'));
//    buttons.forEach(btn => {
//        if (btn.dataset.size === size) btn.classList.add('btn-primary');
//    });
//
//    const addBtn = document.getElementById('addToCartBtn');
//    if (addBtn) addBtn.disabled = !size;
//}
//
//// Add to Cart button logic
//window.addEventListener('DOMContentLoaded', () => {
//    const addBtn = document.getElementById('addToCartBtn');
//    const selectedSizeInput = document.getElementById('selectedSize');
//
//    if (addBtn) {
//        addBtn.addEventListener('click', () => {
//            const productId = document.getElementById('productId').value;
//            const size = selectedSizeInput.value || null;
//
//            if (document.querySelectorAll('.btn-size').length && !size) {
//                alert('Please select a size before adding to cart.');
//                return;
//            }
//
//            if (window.addToCart) {
//                window.addToCart(productId, size, 1);
//            } else {
//                alert('Add to Cart function not available.');
//            }
//        });
//    }
//});
