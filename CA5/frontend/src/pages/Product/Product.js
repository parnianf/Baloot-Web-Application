import React, {useEffect, useState} from 'react';
import Header from "../../components/Header/Header.js";
import Footer from "../../components/Footer/Footer.js";

import './Product.css'
import {useParams} from "react-router-dom";
import {getComments, getCommoditiesSize, getCommodity, rateCommodity} from "../../apis/CommoditiesRequest.js";
import {getProvider} from "../../apis/Provider.js";
import {addToBuyList, getBuyList, removeFromBuyList} from "../../apis/UserRequest.js";

const Comments = () => {
    const {commodityId} = useParams();
    const [commentsList, setCommentsList] = useState([]);

    useEffect(() => {
        getComments(commodityId).then((response) => {
            console.log("Getting Comments")
            let result = [];
            for (let i in response.data) {
                console.log(response.data[i].text);
                result.push(response.data[i]);
            }
            setCommentsList(result);
        }).catch(console.error);
    }, []);


    const CommentTableHeader = () => {
        return (
            <div className="comment-section-title comment-section-title-font">
                Comments &nbsp;
                <div className="comment-count">
                    (<span>{commentsList.length}</span>)
                </div>
            </div>
        )
    }

    const Comment = ({comment}) => {
        return (
            <div className="comment-row">
                <div className="comment-text">
                    {comment.text}
                </div>
                <div className="comment-details">
                    <span>{comment.date}</span>&emsp;&emsp;&#x2022;&emsp;&emsp;#<span>{comment.username}</span>
                </div>
                <div className="comment-voting">
                    Is this comment helpful?
                    &nbsp;
                    <span className="vote-count-text">1</span>
                    <a href="">
                        <img src="/images/thumbsUp.png" alt="thumbsUp img"/>
                    </a>
                    &nbsp;
                    <span className="vote-count-text">1</span>
                    <a href="">
                        <img src="/images/thumbsDown.png" alt="thumbsDown img"/>
                    </a>
                </div>
            </div>
        )
    }

    return (
        <div>
            <CommentTableHeader/>
            <div className="cart-table">
                {commentsList.length > 0 ?
                    commentsList.map((item, index) => (
                        <Comment key={index} comment={item}/>
                    )) : <></>
                }

                <div className="comment-row" id="submit-comment-section">
                    <div className="comment-section-title-font">
                        Submit your opinion
                    </div>
                    <div className="submit-comment-form">
                        <form action="/submit-comment" method="POST">
                            <textarea name="comment"></textarea>
                            <button type="submit">Post</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

    )
}

const ProductInfo = () => {
    const {commodityId} = useParams();
    const [commodity, setCommodity] = useState({});
    const [providerName, setProviderName] = useState("");
    const [buyList, setBuyList] = useState([]);


    useEffect(() => {
        fetchCommodity().then();
        fetchBuyList().then();
    }, [commodityId]);

    const fetchCommodity = async () => {
        await getCommodity(commodityId).then(async (responseCommodity) => {
            setCommodity(responseCommodity.data)
            await getProvider(responseCommodity.data.providerId).then((responseProvider) => {
                setProviderName(responseProvider.data.name);
            }).catch((error) => console.log("Provider Not Found"));
        }).catch((error) => console.log("Commodity Not Found"));
    }

    const extractCommodities = async (data) => {
        const commodities = [];
        for (const [commodityId, quantity] of Object.entries(data)) {
            const CommodityResponse = await getCommodity(commodityId);
            const commodity = {...CommodityResponse.data, quantity: quantity};
            commodities.push(commodity);
        }
        return commodities;
    }
    const fetchBuyList = async () => {
        const buyListResponse = await getBuyList(sessionStorage.getItem('username'));
        const commodities = await extractCommodities(buyListResponse.data);
        setBuyList(commodities);
    }

    const AddCommodityToUsersBuyListButton = () => {
        if (Array.isArray(buyList)) {
            const isCommodityInBuyList = buyList.some((item) => item.id === commodityId.id);
            const buyListItem = buyList.filter(item => item.id === commodity.id).find(item => item);
            if (isCommodityInBuyList && commodity.inStock > 0) {
                return (
                    <div id="add-to-cart-btn" className="product-info-font">
                        <div className="add-remove-buttons" onClick={(e) => handleRemoveFromBuyList(e)}>-</div>
                        <div> {buyListItem.quantity}</div>
                        <div className="add-remove-buttons" onClick={(e) => handleAddToBuyList(e)}>+</div>
                    </div>
                )
            } else if (commodity.inStock > 0) {
                return (
                    <button id="add-to-cart-btn" className="product-info-font" type="button"
                            onClick={(e) => handleAddToBuyList(e)}>add to cart</button>
                )
            }
        }
    }

    const handleAddToBuyList = (e) => {
        e.preventDefault();
        addToBuyList(sessionStorage.getItem('username'), {"id": commodity.id}).then(async (response) => {
            console.log("ADD TO BUY LIST");
            await fetchBuyList();
        }).catch((error) => alert(error.response.data))
    }

    const handleRemoveFromBuyList = (e) => {
        e.preventDefault();
        removeFromBuyList(sessionStorage.getItem('username'), {"id": commodity.id}).then(async (response) => {
            console.log("REMOVE FROM BUY LIST");
            await fetchBuyList();
        }).catch((error) => alert(error.response.data))
    }

    const StarRating = () => {
        const [rating, setRating] = useState(0);
        const [hoverRating, setHoverRating] = useState(0);

        const handleSubmitRate = (e) => {
            e.preventDefault();
            rateCommodity(commodityId, {"username": sessionStorage.getItem('username'), "score": rating})
                .then((response) => {
                    fetchCommodity().then();
                })
                .catch((error) => alert(error.response.data))
        }

        const handleStarClick = (index) => {
            setRating(index + 1);
        };

        const handleStarHover = (index) => {
            setHoverRating(index + 1);
        };

        const handleStarLeave = () => {
            setHoverRating(0);
        };



        return (
            <div className="submit-part">
                <div>
                    <div className="provider-name product-info-font">
                        rate now
                    </div>
                    <div className="stars">
                        <div>
                            {[...Array(10)].map((_, index) => {
                                const starImg = (index < rating || index < hoverRating) ? "star-on.png" : "star-off.png";
                                return (
                                    <img
                                        src={"/images/" + starImg} alt="start"
                                        key={index}
                                        onClick={() => handleStarClick(index)}
                                        onMouseEnter={() => handleStarHover(index)}
                                        onMouseLeave={handleStarLeave}
                                    />
                                )
                            })}
                        </div>
                    </div>
                </div>
                <button id="submit-btn" onClick={(e)=> handleSubmitRate(e)}>
                    submit
                </button>
            </div>
        );
    };

    return (
        <div className="product-info-section">
            <div className="product-img">
                <img src={commodity.image}/>
            </div>
            <div className="product-info">
                <div className="category-part">
                    <div>
                        <div id="product-name">
                            {commodity.name}
                        </div>
                        <div id="stock" className="product-info-font">
                            {commodity.inStock} left in stock
                        </div>
                        <div className="provider-name product-info-font">
                            by <a href={"/providers/" + commodity.providerId}>{providerName}</a>
                        </div>
                        <div id="categories" className="product-info-font">
                            Category(s)
                            <ul>
                                {
                                    commodity.categories?.map((category, index) => (
                                        <li key={index}>
                                            {category}
                                        </li>
                                    ))}
                            </ul>
                        </div>
                    </div>
                    <div id="rating">
                        <img src="/images/star-on.png"/>
                        <div id="rating-font" className="product-info-font">
                            {commodity.rating}
                        </div>
                        {commodity?.usersRating && <div id="number-of-ratings" className="product-info-font">
                            ({Object.keys(commodity.usersRating).length})</div>}
                    </div>
                </div>
                <div className="cart-part">
                    <div id="price" className="product-info-font">
                        {commodity.price}$
                    </div>
                    <AddCommodityToUsersBuyListButton/>
                    {/*<button id="add-to-cart-btn" className="product-info-font">*/}
                    {/*    add to cart*/}
                    {/*</button>*/}
                </div>
                <StarRating/>
            </div>
        </div>
    )
}


const Product = () => {
    //TODO: If not logged in
    return (
        <>
            <Header searchBar={false} username={sessionStorage.getItem('username')}/>
            <main id="main-product">
                <ProductInfo/>
                <Comments/>
            </main>
            <Footer/>
        </>

    )
}

export default Product;